package com.tabletennis.app.domain.competition.mapper;
import org.mapstruct.*;
import com.tabletennis.app.domain.competition.*;
import com.tabletennis.app.domain.competition.dto.*;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface CompetitionMapper { void update(CompetitionRequest request,@MappingTarget Competition entity); }
