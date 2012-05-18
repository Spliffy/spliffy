package org.spliffy.sync.app;

import org.openide.windows.WindowManager;

/**
 *
 * @author brad
 */
public class WindowController {



    public void hideMain() {
        WindowManager.getDefault().getMainWindow().setVisible(false);
    }

    public void showMain() {
        WindowManager.getDefault().getMainWindow().setVisible(true);
    }

    public void showNewAccount() {        

    }

    public void openMediaLounge() {

    }

    public void showRemoteBrowser() {
        throw new UnsupportedOperationException("Not yet implemented");
    }   
}
