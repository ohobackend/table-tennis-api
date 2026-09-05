package com.tabletennis.app.domain.dashboard;
import com.tabletennis.app.domain.match.*;
import com.tabletennis.app.domain.ranking.RankingService;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import java.time.*;
import java.util.Set;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DashboardService {
    private final MatchRepository matches; private final MatchService service; private final RankingService rankings;
    public ApiResponse<?> today(int page,int size) {
        var start=LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay(ZoneId.of("Asia/Seoul")).toOffsetDateTime();
        Specification<Match> spec=(r,q,b)->b.and(b.greaterThanOrEqualTo(r.get("scheduledAt"),start),
            b.lessThan(r.get("scheduledAt"),start.plusDays(1)),r.get("status").in(Status.SCHEDULED,Status.IN_PROGRESS));
        var p=matches.findAll(spec,Queries.page(page,size,"scheduledAt,asc","scheduledAt",Set.of("scheduledAt")));
        return ApiResponse.page(p.map(service::response).getContent(),page,size,p.getTotalElements());
    }
    public ApiResponse<?> recent(int page,int size) {
        var p=matches.findAll(Queries.eq("status",Status.COMPLETED),Queries.page(page,size,"completedAt,desc","completedAt",Set.of("completedAt")));
        return ApiResponse.page(p.map(service::response).getContent(),page,size,p.getTotalElements());
    }
    public ApiResponse<?> top() { return rankings.rankings("month",null,null,1,5); }
}
