package ai.stapi.axonsystem.dynamic.messagehandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.axonframework.common.ObjectUtils;
import org.axonframework.common.ReflectionUtils;
import org.axonframework.common.annotation.AnnotationUtils;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.MessageHandler;
import org.axonframework.spring.config.MessageHandlerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.stereotype.Service;

@Service
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class DynamicMessageHandlerLookup implements BeanDefinitionRegistryPostProcessor {

  private static final Logger logger = Logger.getLogger(
      DynamicMessageHandlerLookup.class.getSimpleName()
  );

  public static List<String> messageHandlerBeans(Class<? extends Message<?>> messageType,
      ConfigurableListableBeanFactory registry) {
    return messageHandlerBeans(messageType, registry, false);
  }

  public static List<String> messageHandlerBeans(Class<? extends Message<?>> messageType,
      ConfigurableListableBeanFactory registry,
      boolean includePrototypeBeans) {
    List<String> found = new ArrayList<>();
    for (String beanName : registry.getBeanDefinitionNames()) {
      BeanDefinition bd = registry.getBeanDefinition(beanName);
      if (includePrototypeBeans || (bd.isSingleton() && !bd.isAbstract())) {
        Class<?> beanType = registry.getType(beanName);
        if (beanType != null && shouldBeRegistered(messageType, beanType)) {
          found.add(beanName);
        }
      }
    }
    return found;
  }

  private static boolean shouldBeRegistered(
      Class<? extends Message<?>> messageType,
      Class<?> beanType
  ) {
    var hasDynamic = false;
    for (Method m : ReflectionUtils.methodsOf(beanType)) {
      var normalAttr = AnnotationUtils.findAnnotationAttributes(m, MessageHandler.class);
      if (normalAttr.isPresent()) {
        return false;
      }
      var dynamicAttr = AnnotationUtils.findAnnotationAttributes(m, DynamicMessageHandler.class);
      if (
          dynamicAttr.isPresent() && messageType.isAssignableFrom(
              (Class<?>) dynamicAttr.get().get("messageType"))
      ) {
        hasDynamic = true;
      }
    }
    return hasDynamic;
  }

  @Override
  public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    if (!(beanFactory instanceof BeanDefinitionRegistry)) {
      logger.warning(
          "Given bean factory is not a BeanDefinitionRegistry. Cannot auto-configure message handlers"
      );
      return;
    }

    for (MessageHandlerConfigurer.Type value : MessageHandlerConfigurer.Type.values()) {
      String configurerBeanName = "DynamicMessageHandlerConfigurer$$Axon$$" + value.name();
      if (beanFactory.containsBeanDefinition(configurerBeanName)) {
        logger.info("Dynamic message handler configurer already available. Skipping configuration");
        break;
      }

      List<String> found = messageHandlerBeans(value.getMessageType(), beanFactory);
      if (!found.isEmpty()) {
        List<String> sortedFound = sortByOrder(found, beanFactory);
        AbstractBeanDefinition beanDefinition =
            BeanDefinitionBuilder.genericBeanDefinition(MessageHandlerConfigurer.class)
                .addConstructorArgValue(value.name())
                .addConstructorArgValue(sortedFound)
                .getBeanDefinition();
        ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(configurerBeanName,
            beanDefinition);
      }
    }
  }
  
  private List<String> sortByOrder(List<String> found,
      @Nonnull ConfigurableListableBeanFactory beanFactory) {
    return found.stream()
        .collect(Collectors.toMap(
            beanRef -> beanRef,
            beanRef -> OrderUtils.getOrder(
                ObjectUtils.getOrDefault(beanFactory.getType(beanRef), Object.class),
                Ordered.LOWEST_PRECEDENCE
            )
        ))
        .entrySet()
        .stream()
        .sorted(java.util.Map.Entry.comparingByValue())
        .map(java.util.Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  @Override
  public void postProcessBeanDefinitionRegistry(@Nonnull BeanDefinitionRegistry registry)
      throws BeansException {
    // No action required.
  }
}
