import request from '@/utils/request'

/**
 * 招聘渠道API
 */
export const recruitChannelApi = {
  // 分页查询渠道
  page(params) {
    return request({
      url: '/recruit/channel/page',
      method: 'get',
      params
    })
  },
  // 查询全部渠道（下拉用）
  list() {
    return request({
      url: '/recruit/channel/list',
      method: 'get'
    })
  },
  // 根据ID查询渠道
  getById(id) {
    return request({
      url: `/recruit/channel/${id}`,
      method: 'get'
    })
  },
  // 新增渠道
  save(data) {
    return request({
      url: '/recruit/channel',
      method: 'post',
      data
    })
  },
  // 更新渠道
  update(id, data) {
    return request({
      url: `/recruit/channel/${id}`,
      method: 'put',
      data
    })
  },
  // 删除渠道
  delete(id) {
    return request({
      url: `/recruit/channel/${id}`,
      method: 'delete'
    })
  },
  // 暂停渠道（需填写暂停原因）
  pause(id, pauseReason) {
    return request({
      url: `/recruit/channel/${id}/pause`,
      method: 'put',
      data: { pauseReason }
    })
  },
  // 恢复渠道
  resume(id) {
    return request({
      url: `/recruit/channel/${id}/resume`,
      method: 'put'
    })
  },
  // 渠道质量回算（实时）
  quality(channelId) {
    return request({
      url: '/recruit/channel/quality',
      method: 'get',
      params: channelId ? { channelId } : {}
    })
  },
  // 执行回算并保存快照
  recalc(channelId) {
    return request({
      url: '/recruit/channel/recalc',
      method: 'post',
      params: channelId ? { channelId } : {}
    })
  },
  // 回算历史快照
  recalcHistory(params) {
    return request({
      url: '/recruit/channel/recalc/history',
      method: 'get',
      params
    })
  }
}

/**
 * 招聘候选人API
 */
export const recruitCandidateApi = {
  // 分页查询候选人
  page(params) {
    return request({
      url: '/recruit/candidate/page',
      method: 'get',
      params
    })
  },
  // 根据ID查询候选人
  getById(id) {
    return request({
      url: `/recruit/candidate/${id}`,
      method: 'get'
    })
  },
  // 新增候选人（报名）
  save(data) {
    return request({
      url: '/recruit/candidate',
      method: 'post',
      data
    })
  },
  // 更新候选人
  update(id, data) {
    return request({
      url: `/recruit/candidate/${id}`,
      method: 'put',
      data
    })
  },
  // 删除候选人
  delete(id) {
    return request({
      url: `/recruit/candidate/${id}`,
      method: 'delete'
    })
  },
  // 推进阶段（interview/trial/onboard/retain/eliminate）
  advanceStage(id, action) {
    return request({
      url: `/recruit/candidate/${id}/stage`,
      method: 'put',
      data: { action }
    })
  },
  // 登记离职（需填写离职原因）
  resign(id, resignReason) {
    return request({
      url: `/recruit/candidate/${id}/resign`,
      method: 'put',
      data: { resignReason }
    })
  }
}
