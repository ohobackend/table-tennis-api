package com.tabletennis.app.domain.board;
import com.tabletennis.app.domain.board.dto.*;
import com.tabletennis.app.domain.board.mapper.*;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.Queries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.Set;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class BoardService {
    private final BoardRepository repository; private final BoardMapper mapper;
    
    public Board require(int id) { return repository.findById(id).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND)); }
    public ApiResponse<?> list(int page,int size,String sort,String keyword) {
        var p=repository.findAll(Queries.search(keyword,"boardTitle","boardContent"),
            Queries.page(page,size,sort,"boardRegDate",Set.of("boardId","boardTitle","boardRegDate")));
        return ApiResponse.page(p.map(mapper::response).getContent(),page,size,p.getTotalElements());
    }
    @Transactional public BoardResponse get(int id) {
        
        return mapper.response(require(id));
    }
    @Transactional public BoardResponse create(BoardRequest r) {
        Board e=new Board(); mapper.update(r,e); e.setBoardRegDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul")));
        
        return mapper.response(repository.save(e));
    }
    @Transactional public BoardResponse update(int id,BoardRequest r) { Board e=require(id); mapper.update(r,e); return mapper.response(e); }
    @Transactional public void delete(int id) { repository.delete(require(id)); }
}
