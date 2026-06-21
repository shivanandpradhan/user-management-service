package com.snp.dev.user_management_service.util;

import com.snp.dev.user_management_service.exception.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;

public class ExceptionUtil {

    /**
     * Checks if the given throwable is one of the handled custom exceptions
     * (excluding the generic Exception case).
     *
     * @param ex the throwable to check
     * @return true if the exception matches one of the known types, false otherwise
     */
    public static boolean isHandledException(Throwable ex) {
        return ex instanceof WebExchangeBindException
                || ex instanceof ResourceNotFoundException
                || ex instanceof BadRequestException
                || ex instanceof ForbiddenException
                || ex instanceof UnauthorizedException
                || ex instanceof AccountLockedException
                || ex instanceof AccountDisabledException
                || ex instanceof AccessDeniedException
                || ex instanceof ResponseStatusException;
    }
}
