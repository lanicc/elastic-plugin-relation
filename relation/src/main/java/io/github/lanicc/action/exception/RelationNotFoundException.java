package io.github.lanicc.action.exception;

import java.io.FileNotFoundException;

/**
 * Created on 2021/9/27.
 *
 * @author lan
 * @since 2.0.0
 */
public class RelationNotFoundException extends FileNotFoundException {
    /**
     * Creates IndexFileNotFoundException with the
     * description message.
     *
     * @param msg
     */
    public RelationNotFoundException(String msg) {
        super(msg);
    }
}
