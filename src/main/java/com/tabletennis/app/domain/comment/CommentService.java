package com.tabletennis.app.domain.comment;
import com.tabletennis.app.domain.comment.dto.CommentRequest;
import com.tabletennis.app.domain.board.BoardService;
import com.tabletennis.app.domain.user.UserService;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.Queries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.LocalDate;
import java.util.Set;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class CommentService {
    private final CommentRepository repository; private final BoardService boards; private final UserService users;
    public ApiResponse<?> list(int boardId,int page,int size,String sort) {
        boards.require(boardId);
        var p=repository.findAll(Queries.eq("boardId",boardId),Queries.page(page,size,sort,"commentRegDate",Set.of("commentId","commentRegDate")));
        return ApiResponse.page(p.getContent(),page,size,p.getTotalElements());
    }
    @Transactional public Comment create(int boardId,CommentRequest r,Jwt jwt) {
        boards.require(boardId); var user=users.require(Integer.parseInt(jwt.getSubject()));
        Comment c=new Comment(); c.setBoardId(boardId); c.setUserId(user.getUserId()); c.setCommentWriter(user.getUserName());
        c.setCommentContent(r.commentContent()); c.setCommentDepth(r.commentDepth()==null?0:r.commentDepth()); c.setCommentRegDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul")));
        return repository.save(c);
    }
    private Comment owned(int id,Jwt jwt) {
        Comment c=repository.findById(id).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND));
        if(!jwt.getSubject().equals(c.getUserId().toString()) && !"ADMIN".equals(jwt.getClaimAsString("role"))) throw new ApiException(ErrorCode.FORBIDDEN);
        return c;
    }
    @Transactional public Comment update(int id,CommentRequest r,Jwt jwt) { Comment c=owned(id,jwt); c.setCommentContent(r.commentContent()); c.setCommentDepth(r.commentDepth()==null?0:r.commentDepth()); return c; }
    @Transactional public void delete(int id,Jwt jwt) { repository.delete(owned(id,jwt)); }
}
