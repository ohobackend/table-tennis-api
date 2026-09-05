package com.tabletennis.app.domain.group;
import com.tabletennis.app.domain.group.dto.GroupRequests.*;
import com.tabletennis.app.domain.competition.*;
import com.tabletennis.app.domain.participant.ParticipantService;
import com.tabletennis.app.domain.user.UserService;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.Queries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class GroupService {
    private final GroupRepository repository; private final GroupParticipantRepository participants;
    private final CompetitionService competitions; private final ParticipantService registrations; private final UserService users;
    private final EntityManager em;
    public Group require(int id) { return repository.findById(id).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND)); }
    public ApiResponse<?> list(int competitionId,int page,int size,String sort) {
        competitions.require(competitionId);
        var p=repository.findAll(Queries.eq("competitionId",competitionId),Queries.page(page,size,sort,"creatDate",Set.of("groupId","groupName","creatDate")));
        return ApiResponse.page(p.getContent(),page,size,p.getTotalElements());
    }
    @Transactional public Group create(int competitionId,Create r) {
        Competition c=competitions.require(competitionId);
        if(!"Y".equals(c.getHasGroups())) throw new ApiException(ErrorCode.CONFLICT,"조 편성을 사용하지 않는 경기 단계입니다.");
        Group g=new Group(); g.setCompetitionId(competitionId); g.setGroupName(r.groupName()); g.setCreatDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); g.setUpDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul")));
        return repository.saveAndFlush(g);
    }
    @Transactional public Group update(int id,Create r) { Group g=require(id); g.setGroupName(r.groupName()); g.setUpDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); return g; }
    public ApiResponse<?> participants(int id,int page,int size,String sort) {
        require(id);
        var p=participants.findAll((root,q,b)->b.equal(root.get("id").get("groupId"),id),
            Queries.page(page,size,sort,"creatDate",Set.of("creatDate","groupRank")));
        return ApiResponse.page(p.map(g->Map.of("groupId",g.getId().getGroupId(),"userId",g.getId().getUserId(),
            "user",users.get(g.getId().getUserId()),"details",g)).getContent(),page,size,p.getTotalElements());
    }
    @Transactional public GroupParticipant add(int id,Participant r) {
        Group g=require(id);
        // Serialize all assignments in one competition to enforce unique membership across groups.
        Competition c=em.find(Competition.class,g.getCompetitionId(),LockModeType.PESSIMISTIC_WRITE);
        users.require(r.userId());
        if(!registrations.registered(c.getTournamentYear(),c.getTournamentId(),r.userId())) throw new ApiException(ErrorCode.CONFLICT,"대회 참가 등록이 필요합니다.");
        long already=((Number)em.createNativeQuery("""
            select count(*) from group_participant gp join "group" g on gp.group_id=g.group_id
            where g.competition_id=:cid and gp.user_id=:uid
            """).setParameter("cid",c.getCompetitionId()).setParameter("uid",r.userId()).getSingleResult()).longValue();
        if(already>0) throw new ApiException(ErrorCode.CONFLICT,"이미 이 경기 단계의 조에 등록된 선수입니다.");
        long count=participants.count((root,q,b)->b.equal(root.get("id").get("groupId"),id));
        if(c.getPlayersPerGroup()!=null && count>=c.getPlayersPerGroup()) throw new ApiException(ErrorCode.CONFLICT,"조 정원을 초과했습니다.");
        GroupParticipant p=new GroupParticipant(); p.setId(new GroupParticipantId(id,r.userId()));
        p.setGroupRank(r.groupRank()); p.setCreatDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); p.setUpDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); return participants.saveAndFlush(p);
    }
    @Transactional public GroupParticipant rank(int id,int userId,Rank r) {
        require(id); var p=participants.findById(new GroupParticipantId(id,userId)).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND));
        p.setGroupRank(r.groupRank()); p.setUpDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))); return p;
    }
}
