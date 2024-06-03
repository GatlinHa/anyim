package com.hibob.anyim.common.exception;

import com.hibob.anyim.common.enums.ServiceErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ServiceException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;

    private ServiceErrorCode serviceErrorCode;
}
