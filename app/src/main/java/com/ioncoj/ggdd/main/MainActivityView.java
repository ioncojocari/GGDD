package com.ioncoj.ggdd.main;

import com.ioncoj.data.models.AccountInfo;
import com.ioncoj.data.utills.IDisplayScore;
import com.ioncoj.data.utills.TaskDisplayer;

public interface MainActivityView extends TaskDisplayer, IDisplayScore{
    void updateUserUi(AccountInfo info);
}
