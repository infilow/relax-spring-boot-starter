package com.infilos.spring.config;

import com.infilos.utils.Loggable;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ShutdownConfigure implements ApplicationListener<ContextClosedEvent>, Loggable {

  private static final List<Runnable> hooks = Lists.mutable.empty();

  public synchronized void register(Runnable hook) {
    if (Objects.nonNull(hook)) {
      hooks.add(hook);
    }
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
    log().info("Start invoke spring shutdown hooks, totally {}...", hooks.size());
    int failed = 0;

    for (Runnable hook : hooks) {
      try {
        hook.run();
      } catch (Throwable e) {
        failed += 1;
        log().error("Invoke spring shutdown hook failed.", e);
      }
    }

    log().info("Finish invoke spring shutdown hooks, totally {}, failed {}.", hooks.size(), failed);
  }
}
