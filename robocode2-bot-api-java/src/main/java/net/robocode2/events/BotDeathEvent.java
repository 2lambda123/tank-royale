package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

/** Event occurring when a bot has died */
@Value
@EqualsAndHashCode(callSuper = true)
public class BotDeathEvent extends GameEvent {
  /** ID of the bot that has died */
  int victimId;

  @Builder
  private BotDeathEvent(int turnNumber, int victimId) {
    this.turnNumber = turnNumber;
    this.victimId = victimId;
  }
}
