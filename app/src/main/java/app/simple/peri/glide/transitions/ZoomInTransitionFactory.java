// ZoomInTransitionFactory.java
package app.simple.peri.glide.transitions;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;
import com.bumptech.glide.request.transition.ViewPropertyTransition;

public class ZoomInTransitionFactory implements TransitionFactory <Drawable> {
    private final ViewPropertyTransition.Animator animator;
    
    public ZoomInTransitionFactory() {
        this.animator = view -> {
            view.setScaleX(0.8f);
            view.setScaleY(0.8f);
            view.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
        };
    }
    
    @Override
    public Transition <Drawable> build(DataSource dataSource, boolean isFirstResource) {
        return new ViewPropertyTransition <>(animator);
    }
}
