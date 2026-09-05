package com.tabletennis.app.domain.tournament.mapper;
import org.mapstruct.*;
import com.tabletennis.app.domain.tournament.*;
import com.tabletennis.app.domain.tournament.dto.*;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface TournamentMapper {
    void update(TournamentRequest request,@MappingTarget Tournament entity);
    @Mapping(target="tournamentYear",source="id.tournamentYear")
    @Mapping(target="tournamentId",source="id.tournamentId")
    TournamentResponse response(Tournament entity);
}
