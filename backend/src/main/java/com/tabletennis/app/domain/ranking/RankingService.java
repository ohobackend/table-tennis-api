package com.tabletennis.app.domain.ranking;
import com.tabletennis.app.domain.ranking.dto.PlayerStats;
import com.tabletennis.app.domain.match.*;
import com.tabletennis.app.domain.user.*;
import com.tabletennis.app.domain.competition.Competition;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class RankingService {
    private final EntityManager em; private final MatchRepository matches; private final MatchService matchService; private final UserService users;
    private OffsetDateTime since(String period) {
        var now=ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return switch(period) {
            case "all" -> OffsetDateTime.parse("1900-01-01T00:00:00+09:00");
            case "month" -> now.withDayOfMonth(1).toLocalDate().atStartOfDay(now.getZone()).toOffsetDateTime();
            case "year" -> now.withDayOfYear(1).toLocalDate().atStartOfDay(now.getZone()).toOffsetDateTime();
            case "week" -> now.toLocalDate().with(java.time.DayOfWeek.MONDAY).atStartOfDay(now.getZone()).toOffsetDateTime();
            default -> throw new ApiException(ErrorCode.VALIDATION_ERROR,"period는 all, week, month, year입니다.");
        };
    }
    public ApiResponse<?> rankings(String period,String club,String gender,int page,int size) {
        Queries.page(page,size,"userId,asc","userId",Set.of("userId"));
        if(gender!=null && !Set.of("M","F").contains(gender.toUpperCase())) throw new ApiException(ErrorCode.VALIDATION_ERROR);
        var rows=aggregate(period,club,gender,null,page,size);
        long total=((Number)em.createNativeQuery("""
            select count(distinct u.user_id) from "user" u join match_participant mp on mp.user_id=u.user_id
            join "match" m on m.match_id=mp.match_id where m.status='COMPLETED' and m.completed_at>=:since
            and (:club='' or u.club_name=:club) and (:gender='' or u.gender=:gender)
            """).setParameter("since",since(period)).setParameter("club",club==null?"":club)
            .setParameter("gender",gender==null?"":gender.toUpperCase()).getSingleResult()).longValue();
        return ApiResponse.page(rows,page,size,total);
    }
    private List<PlayerStats> aggregate(String period,String club,String gender,Integer userId,int page,int size) {
        @SuppressWarnings("unchecked") List<Object[]> rows=em.createNativeQuery("""
            select u.user_id,u.user_name,u.real_name,u.club_name,u.gender,
            count(*) as games,
            sum(case when m.winner_side=mp.side then 1 else 0 end) as wins,
            100.0 * sum(case when m.winner_side=mp.side then 1 else 0 end)/count(*) as rate,
            coalesce(avg(case when mp.side='SIDE_A' then sc.a_points else sc.b_points end),0) as points
            from "user" u join match_participant mp on mp.user_id=u.user_id
            join "match" m on m.match_id=mp.match_id
            left join (select match_id,sum(side_a_point) a_points,sum(side_b_point) b_points from set_score group by match_id) sc on sc.match_id=m.match_id
            where m.status='COMPLETED' and m.completed_at>=:since
            and (:club='' or u.club_name=:club) and (:gender='' or u.gender=:gender)
            and (:uid=0 or u.user_id=:uid)
            group by u.user_id,u.user_name,u.real_name,u.club_name,u.gender
            order by rate desc,games desc,u.user_id asc
            """).setParameter("since",since(period)).setParameter("club",club==null?"":club)
            .setParameter("gender",gender==null?"":gender.toUpperCase()).setParameter("uid",userId==null?0:userId)
            .setFirstResult((page-1)*size).setMaxResults(size).getResultList();
        return rows.stream().map(v->new PlayerStats(((Number)v[0]).intValue(),(String)v[1],(String)v[2],(String)v[3],Objects.toString(v[4],null),
            ((Number)v[5]).longValue(),((Number)v[6]).longValue(),((Number)v[7]).doubleValue(),((Number)v[8]).doubleValue())).toList();
    }
    public PlayerStats stats(int userId) {
        var u=users.get(userId); var list=aggregate("all",null,null,userId,1,1);
        return list.isEmpty()?new PlayerStats(userId,u.userName(),u.realName(),u.clubName(),u.gender(),0,0,0,0):list.get(0);
    }
    public ApiResponse<?> playerMatches(int userId,int page,int size,String sort) {
        users.require(userId);
        Specification<Match> spec=(root,q,b)->{
            var sub=q.subquery(Integer.class); var mp=sub.from(MatchParticipant.class);
            sub.select(mp.get("matchId")).where(b.equal(mp.get("userId"),userId)); return root.get("matchId").in(sub);
        };
        var p=matches.findAll(spec,Queries.page(page,size,sort,"scheduledAt",Set.of("scheduledAt","matchId","completedAt")));
        return ApiResponse.page(p.map(matchService::response).getContent(),page,size,p.getTotalElements());
    }
    public ApiResponse<?> results(String type,String name,String club,int page,int size,String sort) {
        if(!Set.of("individual","team").contains(type)) throw new ApiException(ErrorCode.VALIDATION_ERROR);
        Specification<Match> spec=Queries.eq("status",Status.COMPLETED);
        spec=spec.and((root,q,b)->{
            var sub=q.subquery(Integer.class); var c=sub.from(Competition.class);
            sub.select(c.get("competitionId")).where(type.equals("team")?b.equal(c.get("matchFormat"),MatchFormat.TEAM):c.get("matchFormat").in(MatchFormat.SINGLES,MatchFormat.DOUBLES));
            return root.get("competitionId").in(sub);
        });
        String term=type.equals("team")?club:name;
        if(term!=null && !term.isBlank()) spec=spec.and((root,q,b)->{
            var sub=q.subquery(Integer.class); var mp=sub.from(MatchParticipant.class); var u=sub.from(User.class);
            sub.select(mp.get("matchId")).where(b.equal(mp.get("userId"),u.get("userId")),
                Queries.<User>search(term,type.equals("team")?"clubName":"realName").toPredicate(u,q,b)); return root.get("matchId").in(sub);
        });
        var p=matches.findAll(spec,Queries.page(page,size,sort,"completedAt",Set.of("completedAt","matchId","scheduledAt")));
        return ApiResponse.page(p.map(matchService::response).getContent(),page,size,p.getTotalElements());
    }
}
