package com.example.friend.service.user;

import com.example.common.core.domain.PageQueryDTO;
import com.example.common.core.domain.TableDataInfo;

public interface IUserMessageService {
    TableDataInfo list(PageQueryDTO dto);
}
