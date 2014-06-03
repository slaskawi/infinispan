package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.CommandHandler;
import org.jboss.as.cli.CommandRegistry;
import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The commands interpreted by the Infinispan CLI interpreter with their handlers.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public enum CacheCommand {
   ABORT("abort", -1),
   BEGIN("begin", 1) {
      @Override
      public CommandHandler createHandler(CliCommandBuffer buffer) {
         return new CacheNameArgumentCommandHandler(this, buffer);
      }
   },
   CACHE("cache") {
      @Override
      public CommandHandler createHandler(CliCommandBuffer buffer) {
         return new CacheCommandHandler(buffer);
      }
   },
   CLEAR("clear") {
      @Override
      public CommandHandler createHandler(CliCommandBuffer buffer) {
         return new CacheNameArgumentCommandHandler(this, buffer);
      }
   },
   COMMIT("commit", -1),
   CONTAINER("container") {
      @Override
      public CommandHandler createHandler(CliCommandBuffer buffer) {
         return new ContainerCommandHandler();
      }
   },
   CREATE("create") {
      @Override
      public CommandHandler createHandler(CliCommandBuffer buffer) {
         return new CreateCommandHandler(buffer);
      }
   },
   ENCODING("encoding") {
      @Override
      public CommandHandler createHandler(CliCommandBuffer buffer) {
         return new EncodingCommandHandler(buffer);
      }
   },
   END("end", -1),
   EVICT("evict") {
      @Override
      public CommandHandler createHandler(CliCommandBuffer buffer) {
         return new KeyCommandHandler(EVICT, buffer);
      }
   },
   GET("get") {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new KeyWithCodecCommandHandler(GET, buffer);
      }
   },
   INFO("info") {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new CacheNameArgumentCommandHandler(this, buffer);
      }
   },
   LOCATE("locate") {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new KeyWithCodecCommandHandler(LOCATE, buffer);
      }
   },
   PUT("put") {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new PutCommandHandler(buffer);
      }
   },
   REMOVE("remove") {
      @Override
      public CommandHandler createHandler(CliCommandBuffer buffer) {
         return new KeyCommandHandler(EVICT, buffer);
      }
   },
   REPLACE("replace") {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new ReplaceCommandHandler(buffer);
      }
   },
   ROLLBACK("rollback", -1) {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new CacheNameArgumentCommandHandler(this, buffer);
      }
   },
   SITE("site") {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new SiteCommandHandler(buffer);
      }
   },
   START("start", 1) {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new CacheNameArgumentCommandHandler(this, buffer);
      }
   },
   STATS("stats") {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new StatsCommandHandler(buffer);
      }
   },
   UPGRADE("upgrade") {
      @Override
      protected CommandHandler createHandler(CliCommandBuffer buffer) {
         return new UpgradeCommandHandler(buffer);
      }
   },
   VERSION("version");

   private final String name;
   private final int nesting;

   CacheCommand(String name) {
      this(name, 0);
   }

   CacheCommand(String name, int nesting) {
      this.name = name;
      this.nesting = nesting;
   }

   public String getName() {
      return name;
   }

   public int getNesting() {
      return nesting;
   }

   public final void registerCommandHandler(CommandRegistry commandRegistry, CliCommandBuffer buffer) {
      commandRegistry.registerHandler(createHandler(buffer), true, name);
   }

   public static void registerCacheCommands(CommandRegistry commandRegistry) {
      CliCommandBuffer buffer = new CliCommandBuffer();
      for (CacheCommand command : values()) {
         command.registerCommandHandler(commandRegistry, buffer);
      }
   }

   protected CommandHandler createHandler(CliCommandBuffer buffer) {
      return new NoArgumentsCliCommandHandler(this, buffer);
   }

}
