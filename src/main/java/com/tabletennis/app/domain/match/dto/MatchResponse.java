package com.tabletennis.app.domain.match.dto;
import com.tabletennis.app.common.util.*;
import com.tabletennis.app.domain.match.MatchParticipant;
import com.tabletennis.app.domain.setscore.SetScore;
import java.time.OffsetDateTime;
import java.util.List;
public record MatchResponse(Integer matchId,Integer competitionId,Integer groupId,Integer matchRound,Integer matchNumber,
    Integer courtNumber,Status status,WinnerSide winnerSide,Integer sideASets,Integer sideBSets,Integer totalSets,
    Integer nextMatchId,String notes,OffsetDateTime scheduledAt,String location,OffsetDateTime completedAt,Long version,
    List<MatchParticipant> participants,List<SetScore> sets) {}
