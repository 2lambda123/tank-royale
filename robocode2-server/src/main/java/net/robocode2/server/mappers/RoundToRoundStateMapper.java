package net.robocode2.server.mappers;

import net.robocode2.json_schema.RoundState;
import net.robocode2.model.Round;
import net.robocode2.model.Turn;

public final class RoundToRoundStateMapper {

	public static RoundState map(Round round, Turn turn) {
		RoundState roundState = new RoundState();
		roundState.setRoundNumber(round.getRoundNumber());
		roundState.setTurnNumber(turn.getTurnNumber());
		return roundState;
	}
}
