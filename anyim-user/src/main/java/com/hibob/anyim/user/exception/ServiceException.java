package com.hibob.anyim.user.exception;

import com.hibob.anyim.user.enums.ServiceErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ServiceException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;

    private ServiceErrorCode serviceErrorCode;
}
