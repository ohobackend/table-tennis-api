package com.tabletennis.app.domain.match;
import com.tabletennis.app.domain.match.dto.*;
import com.tabletennis.app.domain.match.mapper.*;
import com.tabletennis.app.domain.competition.*;
import com.tabletennis.app.domain.group.*;
import com.tabletennis.app.domain.participant.ParticipantService;
import com.tabletennis.app.domain.setscore.*;
import com.tabletennis.app.domain.user.UserService;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class MatchService {
    private final MatchRepository repository; private final MatchParticipantRepository participants; private final SetScoreRepository sets;
    private final CompetitionService competitions; private final GroupRepository groups; private final GroupParticipantRepository memberships;
    private final ParticipantService registrations; private final UserService users; private final MatchMapper mapper; private final EntityManager em;
    public Match require(int id) { return repository.findById(id).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND)); }
    public Match lock(int id) { Match m=em.find(Match.class,id,LockModeType.PESSIMISTIC_WRITE); if(m==null) throw new ApiException(ErrorCode.NOT_FOUND); return m; }
    public MatchResponse response(Match m) {
        return mapper.response(m,participants.findAll(Queries.eq("matchId",m.getMatchId()),Sort.by("side","participantOrder")),
            sets.findAll(Queries.eq("matchId",m.getMatchId()),Sort.by("setNumber")));
    }
    public MatchResponse get(int id) { return response(require(id)); }
    public ApiResponse<?> list(int competitionId,int page,int size,String sort) {
        competitions.require(competitionId);
        var p=repository.findAll(Queries.eq("competitionId",competitionId),
            Queries.page(page,size,sort,"matchId",Set.of("matchId","matchRound","matchNumber","scheduledAt")));
        return ApiResponse.page(p.map(this::response).getContent(),page,size,p.getTotalElements());
    }
    private void validate(MatchRequest r,Integer currentId) {
        Competition c=competitions.require(r.competitionId());
        if(c.getStatus()==Status.CANCELLED || c.getStatus()==Status.COMPLETED) throw new ApiException(ErrorCode.CONFLICT,"종료된 경기 단계입니다.");
        if(r.groupId()!=null) {
            Group g=groups.findById(r.groupId()).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND));
            if(!g.getCompetitionId().equals(c.getCompetitionId()) || !"Y".equals(c.getHasGroups())) throw new ApiException(ErrorCode.VALIDATION_ERROR,"다른 경기 단계의 조입니다.");
        } else if("Y".equals(c.getHasGroups())) throw new ApiException(ErrorCode.VALIDATION_ERROR,"조를 선택해야 합니다.");
        Set<Integer> ids=new HashSet<>(); Set<String> slots=new HashSet<>();
        for(var p:r.participants()) {
            users.require(p.userId());
            if(!ids.add(p.userId()) || !slots.add(p.side()+":"+p.participantOrder())) throw new ApiException(ErrorCode.VALIDATION_ERROR,"중복 선수 또는 순서입니다.");
            if(!registrations.registered(c.getTournamentYear(),c.getTournamentId(),p.userId())) throw new ApiException(ErrorCode.CONFLICT,"대회 미등록 선수입니다.");
            if(r.groupId()!=null && !memberships.existsById(new GroupParticipantId(r.groupId(),p.userId()))) throw new ApiException(ErrorCode.CONFLICT,"조에 등록되지 않은 선수입니다.");
        }
        long a=r.participants().stream().filter(p->p.side()==Side.SIDE_A).count();
        long b=r.participants().size()-a;
        int count=c.getMatchFormat()==MatchFormat.SINGLES?1:2;
        if(c.getMatchFormat()==MatchFormat.TEAM?(a<3 || b<3):(a!=count || b!=count)) throw new ApiException(ErrorCode.VALIDATION_ERROR,"경기 방식에 맞지 않는 참가 인원입니다.");
        if(r.nextMatchId()!=null) {
            Set<Integer> visited=new HashSet<>(); Integer cursor=r.nextMatchId();
            while(cursor!=null) {
                if(Objects.equals(cursor,currentId) || !visited.add(cursor)) throw new ApiException(ErrorCode.VALIDATION_ERROR,"대진표 순환 연결입니다.");
                Match next=require(cursor);
                if(!next.getCompetitionId().equals(r.competitionId())) throw new ApiException(ErrorCode.VALIDATION_ERROR,"다음 경기는 같은 단계여야 합니다.");
                cursor=next.getNextMatchId();
            }
        }
    }
    private void assign(Match m,MatchRequest r) {
        for(var p:r.participants()) {
            MatchParticipant mp=new MatchParticipant(); mp.setMatchId(m.getMatchId()); mp.setUserId(p.userId());
            mp.setSide(p.side()); mp.setParticipantOrder(p.participantOrder()); mp.setCreatDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); mp.setUpDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); participants.save(mp);
        }
    }
    @Transactional public MatchResponse create(MatchRequest r) {
        validate(r,null); Match m=new Match(); mapper.update(r,m); m.setStatus(Status.SCHEDULED); m.setSideASets(0); m.setSideBSets(0); m.setTotalSets(0);
        repository.saveAndFlush(m); assign(m,r); return response(m);
    }
    @Transactional public MatchResponse update(int id,MatchRequest r) {
        Match m=lock(id);
        if(m.getStatus()==Status.COMPLETED || sets.count(Queries.eq("matchId",id))>0) throw new ApiException(ErrorCode.CONFLICT,"점수가 등록된 경기의 대진은 변경할 수 없습니다.");
        if(!m.getCompetitionId().equals(r.competitionId())) throw new ApiException(ErrorCode.VALIDATION_ERROR,"경기 단계는 변경할 수 없습니다.");
        validate(r,id); mapper.update(r,m); participants.deleteAll(participants.findAll(Queries.eq("matchId",id))); participants.flush(); assign(m,r); return response(m);
    }
    @Transactional public void delete(int id) { repository.delete(lock(id)); }
}
