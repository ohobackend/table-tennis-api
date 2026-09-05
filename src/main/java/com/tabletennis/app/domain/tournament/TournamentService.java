package com.tabletennis.app.domain.tournament;
import com.tabletennis.app.domain.tournament.dto.*;
import com.tabletennis.app.domain.tournament.mapper.TournamentMapper;
import com.tabletennis.app.domain.user.UserService;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.Queries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import java.util.Set;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class TournamentService {
    private final TournamentRepository repository; private final TournamentMapper mapper; private final UserService users; private final EntityManager em;
    public Tournament require(int year,int id) { return repository.findById(new TournamentId(year,id)).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND)); }
    public TournamentResponse get(int year,int id) { return mapper.response(require(year,id)); }
    public ApiResponse<?> list(int page,int size,String sort,String keyword) {
        var p=repository.findAll(Queries.search(keyword,"tournamentName","location"),
            Queries.page(page,size,sort,"startDate",Set.of("startDate","endDate","tournamentName")));
        return ApiResponse.page(p.map(mapper::response).getContent(),page,size,p.getTotalElements());
    }
    private void validate(TournamentRequest r) {
        if(r.endDate().isBefore(r.startDate())) throw new ApiException(ErrorCode.VALIDATION_ERROR,"종료일은 시작일 이후여야 합니다.");
        if(r.organizerId()!=null) users.require(r.organizerId());
    }
    @Transactional public TournamentResponse create(TournamentRequest r) {
        validate(r); Tournament t=new Tournament();
        t.setId(new TournamentId(r.tournamentYear(),((Number)em.createNativeQuery("select nextval('tournament_id_seq')").getSingleResult()).intValue()));
        mapper.update(r,t); return mapper.response(repository.save(t));
    }
    @Transactional public TournamentResponse update(int year,int id,TournamentRequest r) {
        if(r.tournamentYear()!=year) throw new ApiException(ErrorCode.VALIDATION_ERROR,"대회 연도는 변경할 수 없습니다.");
        validate(r); Tournament t=require(year,id); mapper.update(r,t); return mapper.response(t);
    }
    @Transactional public void delete(int year,int id) { repository.delete(require(year,id)); }
}
