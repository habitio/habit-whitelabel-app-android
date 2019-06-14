package com.muzzley.app.shortcuts;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.muzzley.R;
import com.muzzley.model.shortcuts.Shortcut;
import com.muzzley.model.shortcuts.ShortcutSuggestions;
import com.muzzley.model.shortcuts.Shortcuts;
import com.muzzley.util.Utils;
import com.muzzley.util.retrofit.MuzzleyCoreService;
import com.muzzley.util.retrofit.UserService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by kyryloryabin on 03/01/16.
 */
public class ShortcutsController {

    @Inject UserService userService;
    @Inject MuzzleyCoreService muzzleyCoreService;
//    @Inject @Named("mock") UserService userServiceMock;

    @Inject public ShortcutsController(){}

    public void getShortcuts(Consumer<List<Shortcut>[]> onDone, Consumer<Throwable> onError) {
        Observable.zip(
                userService.getShortcuts(),
//                userServiceMock.getShortcuts(),
                userService.getShortcutsSuggestions(),
//                userServiceMock.getShortcutsSuggestions(),
                new BiFunction<Shortcuts, ShortcutSuggestions, List<Shortcut>[]>() {

                    @Override
                    public List<Shortcut>[] apply(Shortcuts shortcuts, ShortcutSuggestions shortcutSuggestions) {
                        List<Shortcut>[] result = new List[2];
                        result[0] = new ArrayList<>();
                        if (shortcuts != null) {
                            result[0].addAll(shortcuts.getShortcuts());
                        }

                        result[1] = new ArrayList<>();
                        if (shortcutSuggestions != null) {
                            result[1].addAll(shortcutSuggestions.getShortcuts());
                        }

                        return result;
                    }

                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onDone, onError);
    }

    public Completable executeShortcut(String shortcutId) {
        return userService.executeShortcut(shortcutId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void deleteShortcut(String shortcutId, Consumer<Shortcut> onDone, Consumer<Throwable> onError) {
        userService.deleteShortcut(shortcutId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onDone, onError);
    }

    public void reorderShortcuts(List<String> ids, Consumer<String> onDone, Consumer<Throwable> onError) {
        JsonArray jsonids = new JsonArray();
        for (String orderedId : ids) {
            jsonids.add(new JsonPrimitive(orderedId));
        }

        JsonObject order = new JsonObject();
        order.add("order", jsonids);

        userService.reorderShortcutsOld(order)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onDone, onError);
    }

    public void startDragFeedback(Context ctx, View shortcut) {
        final View background = shortcut.findViewById(R.id.bg_shortcut);
        final View btnExecute = shortcut.findViewById(R.id.btn_execute);
        ValueAnimator buttonAnimation = ValueAnimator.ofInt(background.getHeight(),
                background.getHeight() + (int) Utils.dpToPx(ctx, 10));
        buttonAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams bgParams = background.getLayoutParams();
                ViewGroup.LayoutParams buttonParams = background.getLayoutParams();
                bgParams.width = buttonParams.width = value;
                bgParams.height = buttonParams.height = value;
                background.setLayoutParams(bgParams);
                btnExecute.setLayoutParams(buttonParams);
            }
        });

        final View shadow = shortcut.findViewById(R.id.bg_shadow);
        ValueAnimator shadowAnimation = ValueAnimator.ofInt(shadow.getHeight(),
                shadow.getHeight() + (int) Utils.dpToPx(ctx, 10));
        shadowAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams shadowParams = shadow.getLayoutParams();
                shadowParams.width = value;
                shadowParams.height = value;
                shadow.setLayoutParams(shadowParams);
            }
        });

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.playTogether(buttonAnimation, shadowAnimation);
        animationSet.setInterpolator(new BounceInterpolator());
        animationSet.start();
    }

    public void endDragFeedback(Context ctx, View shortcut) {
        final View background = shortcut.findViewById(R.id.bg_shortcut);
        final View btnExecute = shortcut.findViewById(R.id.btn_execute);
        ValueAnimator buttonAnimation = ValueAnimator.ofInt(background.getHeight(),
                (int) ctx.getResources().getDimension(R.dimen.widget_shortcut_btn_size));
        buttonAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams bgParams = background.getLayoutParams();
                ViewGroup.LayoutParams buttonParams = background.getLayoutParams();
                bgParams.width = buttonParams.width = value;
                bgParams.height = buttonParams.height = value;
                background.setLayoutParams(bgParams);
                btnExecute.setLayoutParams(buttonParams);
            }
        });

        final View shadow = shortcut.findViewById(R.id.bg_shadow);
        ValueAnimator shadowAnimation = ValueAnimator.ofInt(shadow.getHeight(),
                (int) ctx.getResources().getDimension(R.dimen.widget_shortcut_shadow));
        shadowAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams shadowParams = shadow.getLayoutParams();
                shadowParams.width = value;
                shadowParams.height = value;
                shadow.setLayoutParams(shadowParams);
            }
        });

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.playTogether(buttonAnimation, shadowAnimation);
        animationSet.setInterpolator(new BounceInterpolator());
        animationSet.start();
    }
}
