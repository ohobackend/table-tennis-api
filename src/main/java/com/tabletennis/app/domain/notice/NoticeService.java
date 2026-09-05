package com.tabletennis.app.domain.notice;
import com.tabletennis.app.domain.notice.dto.*;
import com.tabletennis.app.domain.notice.mapper.*;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.util.Queries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.Set;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class NoticeService {
    private final NoticeRepository repository; private final NoticeMapper mapper;
    private final jakarta.persistence.EntityManager em;
    public Notice require(int id) { return repository.findById(id).orElseThrow(()->new ApiException(ErrorCode.NOTICE_NOT_FOUND)); }
    public ApiResponse<?> list(int page,int size,String sort,String keyword) {
        var p=repository.findAll(Queries.search(keyword,"noticeTitle","noticeContents"),
            Queries.page(page,size,sort,"notiRegDate",Set.of("noticeNum","noticeTitle","notiRegDate")));
        return ApiResponse.page(p.map(mapper::response).getContent(),page,size,p.getTotalElements());
    }
    @Transactional public NoticeResponse get(int id) {
        int changed=em.createQuery("update Notice n set n.hitNum=n.hitNum+1 where n.noticeNum=:id").setParameter("id",id).executeUpdate(); if(changed==0) throw new ApiException(ErrorCode.NOTICE_NOT_FOUND); em.clear();
        return mapper.response(require(id));
    }
    @Transactional public NoticeResponse create(NoticeRequest r) {
        Notice e=new Notice(); mapper.update(r,e); e.setNotiRegDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul")));
        e.setHitNum(0);
        return mapper.response(repository.save(e));
    }
    @Transactional public NoticeResponse update(int id,NoticeRequest r) { Notice e=require(id); mapper.update(r,e); return mapper.response(e); }
    @Transactional public void delete(int id) { repository.delete(require(id)); }
}
