using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when game has just ended.
  /// </summary>
  public sealed class GameEndedEvent : IMessage
  {
    /// <summary>Number of rounds played.</summary>
    public int NumberOfRounds { get; }

    /// <summary>Results of the battle.</summary>
    public IEnumerable<BotResults> Results { get; }

    /// <summary>
    /// Initializes a new instance of the GameEndedEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="victimId">Id of the bot that has died.</param>
    [JsonConstructor]
    public GameEndedEvent(int numberOfRounds, IEnumerable<BotResults> results) : base() =>
      (NumberOfRounds, Results) = (numberOfRounds, results);
  }
}