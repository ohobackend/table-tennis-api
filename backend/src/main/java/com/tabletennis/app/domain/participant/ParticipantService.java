package com.tabletennis.app.domain.participant;
import com.tabletennis.app.domain.participant.dto.*;
import com.tabletennis.app.domain.tournament.TournamentService;
import com.tabletennis.app.domain.user.*;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.Queries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ParticipantService {
    private final TournamentParticipantRepository repository; private final TournamentService tournaments; private final UserService users;
    private final UserRepository userRepository;
    public boolean registered(int year,int tournamentId,int userId) {
        return repository.count(Queries.<TournamentParticipant>eq("tournamentYear",year).and(Queries.eq("tournamentId",tournamentId)).and(Queries.eq("userId",userId)))>0;
    }
    private ParticipantResponse response(TournamentParticipant p) {
        return new ParticipantResponse(p.getParticipantId(),p.getTournamentYear(),p.getTournamentId(),users.get(p.getUserId()),p.getRegiDate(),p.getFinalRank(),p.getNotes(),p.getUpDate());
    }
    public ApiResponse<?> list(int year,int id,int page,int size,String sort,String keyword) {
        tournaments.require(year,id);
        Specification<TournamentParticipant> spec=Queries.<TournamentParticipant>eq("tournamentYear",year).and(Queries.eq("tournamentId",id));
        if(keyword!=null && !keyword.isBlank()) spec=spec.and((root,query,b)->{
            var sub=query.subquery(Integer.class); var user=sub.from(User.class);
            sub.select(user.get("userId")).where(Queries.<User>search(keyword,"userName","realName","clubName").toPredicate(user,query,b));
            return root.get("userId").in(sub);
        });
        var p=repository.findAll(spec,Queries.page(page,size,sort,"regiDate",Set.of("participantId","regiDate","finalRank")));
        return ApiResponse.page(p.map(this::response).getContent(),page,size,p.getTotalElements());
    }
    @Transactional public ParticipantResponse create(int year,int id,ParticipantRequest r) {
        tournaments.require(year,id); users.require(r.userId());
        if(registered(year,id,r.userId())) throw new ApiException(ErrorCode.CONFLICT);
        TournamentParticipant p=new TournamentParticipant(); p.setTournamentYear(year); p.setTournamentId(id); p.setUserId(r.userId());
        p.setRegiDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); p.setUpDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); p.setFinalRank(r.finalRank()); p.setNotes(r.notes());
        return response(repository.saveAndFlush(p));
    }
    private TournamentParticipant require(int year,int id,int participantId) {
        tournaments.require(year,id);
        TournamentParticipant p=repository.findById(participantId).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND));
        if(p.getTournamentYear()!=year || p.getTournamentId()!=id) throw new ApiException(ErrorCode.NOT_FOUND);
        return p;
    }
    @Transactional public ParticipantResponse update(int year,int id,int participantId,ParticipantRequest r) {
        TournamentParticipant p=require(year,id,participantId);
        if(!p.getUserId().equals(r.userId())) throw new ApiException(ErrorCode.VALIDATION_ERROR,"참가 선수는 변경할 수 없습니다.");
        p.setFinalRank(r.finalRank()); p.setNotes(r.notes()); p.setUpDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); return response(p);
    }
    @Transactional public void delete(int year,int id,int participantId) {
        TournamentParticipant p=require(year,id,participantId);
        long assigned=((Number)em.createNativeQuery("""
            select (select count(*) from group_participant gp join "group" g on g.group_id=gp.group_id
            join competition c on c.competition_id=g.competition_id where c.tournament_year=:year and c.tournament_id=:tid and gp.user_id=:uid)
            + (select count(*) from match_participant mp join "match" m on m.match_id=mp.match_id
            join competition c on c.competition_id=m.competition_id where c.tournament_year=:year and c.tournament_id=:tid and mp.user_id=:uid)
            """).setParameter("year",year).setParameter("tid",id).setParameter("uid",p.getUserId()).getSingleResult()).longValue();
        if(assigned>0) throw new ApiException(ErrorCode.CONFLICT,"조 또는 경기에 배정된 참가자는 취소할 수 없습니다.");
        repository.delete(p);
    }
    private final jakarta.persistence.EntityManager em;
}
