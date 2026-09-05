package com.tabletennis.app.domain.competition;
import com.tabletennis.app.domain.competition.dto.*;
import com.tabletennis.app.domain.competition.mapper.*;
import com.tabletennis.app.domain.tournament.TournamentService;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class CompetitionService {
    private final CompetitionRepository repository; private final TournamentService tournaments; private final CompetitionMapper mapper;
    private final com.tabletennis.app.domain.match.MatchRepository matches; private final com.tabletennis.app.domain.group.GroupRepository groups;
    public Competition require(int id) { return repository.findById(id).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND)); }
    public ApiResponse<?> list(int year,int id,int page,int size,String sort) {
        tournaments.require(year,id); var p=repository.findAll(Queries.<Competition>eq("tournamentYear",year).and(Queries.eq("tournamentId",id)),
            Queries.page(page,size,sort,"competitionOrder",Set.of("competitionId","competitionOrder","competitionName")));
        return ApiResponse.page(p.getContent(),page,size,p.getTotalElements());
    }
    private void validate(CompetitionRequest r) {
        if("Y".equals(r.hasGroups()) && r.playersPerGroup()==null) throw new ApiException(ErrorCode.VALIDATION_ERROR,"조별 인원수가 필요합니다.");
    }
    @Transactional public Competition create(int year,int id,CompetitionRequest r) {
        tournaments.require(year,id); validate(r); Competition c=new Competition(); c.setTournamentYear(year); c.setTournamentId(id); mapper.update(r,c); return repository.save(c);
    }
    @Transactional public Competition update(int id,CompetitionRequest r) {
        validate(r); Competition c=require(id);
        if((matches.count(Queries.eq("competitionId",id))>0 || groups.count(Queries.eq("competitionId",id))>0)
            && (c.getMatchFormat()!=r.matchFormat() || !Objects.equals(c.getHasGroups(),r.hasGroups()) || !Objects.equals(c.getPlayersPerGroup(),r.playersPerGroup()) || c.getCompetitionType()!=r.competitionType()))
            throw new ApiException(ErrorCode.CONFLICT,"조나 경기가 있는 단계의 경기 방식을 변경할 수 없습니다.");
        mapper.update(r,c); return c;
    }
    @Transactional public void delete(int id) { repository.delete(require(id)); }
}
