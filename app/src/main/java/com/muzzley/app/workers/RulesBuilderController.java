package com.muzzley.app.workers;

import com.google.gson.Gson;
import com.muzzley.model.workers.RuleUnitResponse;
import com.muzzley.model.workers.WorkerUnit;
import com.muzzley.model.shortcuts.Shortcut;
import com.muzzley.util.Controller;
import com.muzzley.util.retrofit.UserService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by kyryloryabin on 17/02/16.
 */
public class RulesBuilderController extends Controller<RulesBuilder> {

    @Inject UserService userService;
    @Inject Gson gson;

    @Inject public RulesBuilderController(){}

    public void createShortcut(Shortcut shortcut) {
        userService.createShortcut(shortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Shortcut>() {
                    @Override
                    public void accept(Shortcut shortcut) {
                        if (getControlled() != null) {
                            getControlled().successSendData(gson.toJson(shortcut));
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (getControlled() != null) {
                            getControlled().failSendData();
                        }

                    }
                });
    }

    public void editShortcut(Shortcut shortcut) {
        String shortcutId = shortcut.getId();
        shortcut.setId(null);
        shortcut.setExecute(null);
        userService.editShortcut(shortcutId,shortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Shortcut>() {
                    @Override
                    public void accept(Shortcut shortcut) {
                        if (getControlled() != null) {
                            getControlled().successSendData(gson.toJson(shortcut));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (getControlled() != null) {
                            getControlled().failSendData();
                        }
                    }
                });
    }

    public Shortcut buildShortcut(String label, boolean showInWatch, List<RuleUnitResponse> ruleAction) {
        List<WorkerUnit> actions = null;
        if(ruleAction != null && !ruleAction.isEmpty()) {
            actions = new ArrayList<>();
            for (RuleUnitResponse rulesActions : ruleAction) {
                for (WorkerUnit rule : rulesActions.getRuleUnit().getRules()) {
                    actions.add(rule);
                }
            }
        }

        Shortcut shortcut = new Shortcut(label, showInWatch, actions);
        return shortcut;
    }

}
