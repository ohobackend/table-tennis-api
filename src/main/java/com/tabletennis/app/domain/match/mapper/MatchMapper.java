package com.tabletennis.app.domain.match.mapper;
import org.mapstruct.*;
import com.tabletennis.app.domain.match.*;
import com.tabletennis.app.domain.match.dto.*;
import com.tabletennis.app.domain.setscore.SetScore;
import java.util.List;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface MatchMapper {
    void update(MatchRequest request,@MappingTarget Match entity);
    MatchResponse response(Match entity,List<MatchParticipant> participants,List<SetScore> sets);
}
