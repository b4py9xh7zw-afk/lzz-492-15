package com.scaffolding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scaffolding.entity.RecruitCandidate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 招聘候选人Mapper接口
 *
 * @author scaffolding
 */
@Mapper
public interface RecruitCandidateMapper extends BaseMapper<RecruitCandidate> {
}
