package com.example.anarg.openmap2;

/**
 * This interface provides the method process finish which can be overridden to display the output
 * of the async task.
 * @author Anarghya Das
 */
public interface AsyncResponse {
    /**
     * @param output Output of the async Task (VS Code)
     */
    void processFinish(String output);
}