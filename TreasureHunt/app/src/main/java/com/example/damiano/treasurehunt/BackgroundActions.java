package com.example.damiano.treasurehunt;

/**
 * Created by Lorenzo on 29/08/2016.
 */
public interface BackgroundActions {
    public boolean BackgroundActions(String response);
    public void PostExecuteActions(Boolean success);
    public void CancelledActions();
}
