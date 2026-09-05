package com.tabletennis.app.common.util;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import com.tabletennis.app.common.exception.*;
import java.util.*;
public final class Queries {
    private Queries() {}
    public static Pageable page(int page,int size,String sort,String defaultField,Set<String> allowed) {
        if(page<1 || size<1 || size>100) throw new ApiException(ErrorCode.VALIDATION_ERROR,"page는 1 이상, size는 1~100입니다.");
        String[] parts=sort.split(",",-1);
        String field=parts[0].equals("reg_date")?defaultField:parts[0];
        if(parts.length!=2 || !allowed.contains(field) || !Set.of("asc","desc").contains(parts[1].toLowerCase()))
            throw new ApiException(ErrorCode.VALIDATION_ERROR,"지원하지 않는 정렬입니다.");
        return PageRequest.of(page-1,size,Sort.by(Sort.Direction.fromString(parts[1]),field));
    }
    public static <T> Specification<T> eq(String field,Object value) { return (r,q,b)->b.equal(r.get(field),value); }
    public static <T> Specification<T> search(String keyword,String... fields) {
        return (r,q,b)->{
            if(keyword==null || keyword.isBlank()) return b.conjunction();
            String term="%"+keyword.toLowerCase(Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
            return b.or(Arrays.stream(fields).map(f->b.like(b.lower(r.get(f)),term,'\\')).toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
