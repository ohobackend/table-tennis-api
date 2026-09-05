package com.tabletennis.app.domain.user;
import com.tabletennis.app.domain.user.dto.*;
import com.tabletennis.app.domain.user.mapper.UserMapper;
import com.tabletennis.app.common.response.*;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.util.Queries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class UserService {
    private final UserRepository repository; private final UserMapper mapper;
    public User require(int id) { return repository.findById(id).orElseThrow(()->new ApiException(ErrorCode.NOT_FOUND)); }
    public UserProfile get(int id) { return mapper.profile(require(id)); }
    public ApiResponse<?> list(int page,int size,String sort,String keyword,String club,String gender) {
        Specification<User> spec=Queries.search(keyword,"userName","realName");
        if(club!=null) spec=spec.and(Queries.eq("clubName",club));
        if(gender!=null) {
            if(!Set.of("M","F").contains(gender.toUpperCase())) throw new ApiException(ErrorCode.VALIDATION_ERROR);
            spec=spec.and(Queries.eq("gender",gender.toUpperCase()));
        }
        var result=repository.findAll(spec,Queries.page(page,size,sort,"userId",Set.of("userId","userName","realName")));
        return ApiResponse.page(result.map(mapper::profile).getContent(),page,size,result.getTotalElements());
    }
    @Transactional public UserProfile update(int id,UserUpdateRequest r,Jwt jwt) {
        if(!jwt.getSubject().equals(Integer.toString(id)) && !"ADMIN".equals(jwt.getClaimAsString("role"))) throw new ApiException(ErrorCode.FORBIDDEN);
        User u=require(id); mapper.update(r,u); return mapper.profile(u);
    }
}
