package com.tabletennis.app.domain.setscore;
import com.tabletennis.app.domain.setscore.dto.SetRequests.*;
import com.tabletennis.app.domain.match.*;
import com.tabletennis.app.domain.match.dto.MatchResponse;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.time.*;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class SetScoreService {
    private final SetScoreRepository repository; private final MatchService matches;
    @Value("${match.sets-to-win}") private int setsToWin;
    @jakarta.annotation.PostConstruct void validateConfig() {
        if(setsToWin<2 || setsToWin>3) throw new IllegalArgumentException("match.sets-to-win must be 2 or 3");
    }
    public List<SetScore> list(int matchId) { matches.require(matchId); return scores(matchId); }
    private List<SetScore> scores(int matchId) { return repository.findAll(Queries.eq("matchId",matchId),Sort.by("setNumber")); }
    private void points(int a,int b) {
        int high=Math.max(a,b),low=Math.min(a,b);
        if(!((high==11 && low<=9) || (high>11 && high-low==2))) throw new ApiException(ErrorCode.VALIDATION_ERROR,"세트는 11점 이상, 2점 차로 종료되어야 합니다.");
    }
    private void editable(Match m) {
        if(m.getStatus()==Status.CANCELLED) throw new ApiException(ErrorCode.CONFLICT,"취소된 경기입니다.");
    }
    @Transactional public MatchResponse add(int matchId,Batch r) {
        Match m=matches.lock(matchId); editable(m);
        if(m.getStatus()==Status.COMPLETED) throw new ApiException(ErrorCode.CONFLICT,"완료된 경기에는 세트를 추가할 수 없습니다.");
        Set<Integer> numbers=new HashSet<>();
        for(var s:scores(matchId)) numbers.add(s.getSetNumber());
        for(Score s:r.sets()) {
            if(!numbers.add(s.setNumber())) throw new ApiException(ErrorCode.CONFLICT,"이미 등록된 세트 번호입니다.");
            points(s.sideAPoint(),s.sideBPoint()); SetScore e=new SetScore(); e.setMatchId(matchId);
            copy(s,e); repository.save(e);
        }
        repository.flush(); recalculate(m); return matches.response(m);
    }
    @Transactional public MatchResponse update(int matchId,int setId,Score r) {
        Match m=matches.lock(matchId); editable(m);
        SetScore e=repository.findById(setId).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND));
        if(e.getMatchId()!=matchId) throw new ApiException(ErrorCode.NOT_FOUND);
        if(!e.getSetNumber().equals(r.setNumber())) throw new ApiException(ErrorCode.VALIDATION_ERROR,"세트 번호는 변경할 수 없습니다.");
        points(r.sideAPoint(),r.sideBPoint()); copy(r,e); repository.flush(); recalculate(m); return matches.response(m);
    }
    @Transactional public MatchResponse finalizeMatch(int matchId) {
        Match m=matches.lock(matchId); editable(m); recalculate(m);
        if(m.getWinnerSide()==null) throw new ApiException(ErrorCode.CONFLICT,"승리 조건을 충족하지 않았습니다.");
        return matches.response(m);
    }
    private void copy(Score r,SetScore e) { e.setSetNumber(r.setNumber()); e.setSideAPoint(r.sideAPoint()); e.setSideBPoint(r.sideBPoint()); }
    private void recalculate(Match m) {
        int a=0,b=0,number=1;
        for(SetScore s:scores(m.getMatchId())) {
            if(s.getSetNumber()!=number++ || a==setsToWin || b==setsToWin) throw new ApiException(ErrorCode.VALIDATION_ERROR,"세트 순서가 누락되었거나 승부 종료 후 세트가 있습니다.");
            points(s.getSideAPoint(),s.getSideBPoint());
            if(s.getSideAPoint()>s.getSideBPoint()) a++; else b++;
        }
        m.setSideASets(a); m.setSideBSets(b); m.setTotalSets(a+b);
        if(a==setsToWin || b==setsToWin) {
            m.setWinnerSide(a>b?WinnerSide.SIDE_A:WinnerSide.SIDE_B); m.setStatus(Status.COMPLETED);
            if(m.getCompletedAt()==null) m.setCompletedAt(OffsetDateTime.now(ZoneId.of("Asia/Seoul")));
        } else {
            m.setWinnerSide(null); m.setCompletedAt(null); m.setStatus(a+b==0?Status.SCHEDULED:Status.IN_PROGRESS);
        }
    }
}
