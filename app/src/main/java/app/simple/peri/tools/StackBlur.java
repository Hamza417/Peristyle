package app.simple.peri.tools;

import android.graphics.Bitmap;

import java.util.Arrays;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

/**
 * This is copied from <a href="https://stackoverflow.com/a/10028267/3050249">...</a>
 * and optimized for less memory consumption & better locality, and creates less GC pressure.
 * {@code int[] dv} division memoization was removed: on my load it had 700k elements and gave no speedup.
 * <a href="<p>
 * ">* This gist: https://gist.github.com/Miha-x64/3fb489d13dbf69</a>e1611a8fb688b57<a href="d3d
 * ">* Superseded by my library: http</a>s://github.com/Miha-x64/Lubricant
 * <p>
 * This class is *not* thread safe: it reuses some buffers.
 *
 * @author Mike Gorünov
 */
@SuppressWarnings ("SpellCheckingInspection")
public final class StackBlur {
    
    private int[] pix;
    private int[] rgb;
    private int[] vmin;
    private int[] stack;
    
    public StackBlur() {
    }
    
    /**
     * Stack Blur v1.0 from
     * <a href="http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html">...</a>
     * Java Author: Mario Klingemann <mario at quasimondo.com>
     * <a href="http://incubator.quasimondo.com">...</a>
     * <p>
     * created February 29, 2004
     * Android port : Yahel Bouaziz <yahel at ka<a href="yenko.com>
     * ">* http</a>://www.kayenko.com
     * ported april 5th, 2012
     * <p>
     * This is a compromise between Gaussian Blur and Box blur
     * It creates much better looking blurs than Box Blur, but is
     * 7x faster than my Gaussian Blur implementation.
     * <p>
     * I called it Stack Blur because this describes best how this
     * filter works internally: it creates a kind of moving stack
     * of colors whilst scanning through the image. Thereby it
     * just has to add one new block of color to the right side
     * of the stack and remove the leftmost color. The remaining
     * colors on the topmost layer of the stack are either added on
     * or reduced by one, depending on if they are on the right or
     * on the left side of the stack.
     * <p>
     * If you are using this algorithm in your code please add
     * the following line:
     * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
     */
    public void blurRgb(@NonNull Bitmap bitmap, @IntRange (from = 1) int radius) {
        int w = bitmap.getWidth(); // implicit null-check
        int h = bitmap.getHeight();
        if (radius < 1) {
            throw new IllegalArgumentException("radius < 1: " + radius);
        }
        
        int[] pix, rgb, vmin, stack;
        
        int wh = w * h;
        if ((pix = this.pix) == null || pix.length < wh) {
            this.pix = pix = new int[wh];
            this.rgb = rgb = new int[wh];
        } else {
            // Bitmap#getPixels will fill pix[]
            Arrays.fill(rgb = this.rgb, 0);
        }
        
        if ((vmin = this.vmin) == null || vmin.length < Math.max(w, h)) {
            this.vmin = vmin = new int[Math.max(w, h)];
        } else {
            Arrays.fill(vmin, 0);
        }
        
        int div = radius + radius + 1;
        if ((stack = this.stack) == null || stack.length < div) {
            this.stack = stack = new int[div];
        } else {
            Arrays.fill(stack, 0);
        }
        
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        
        yw = yi = 0;
        
        int prt, start;
        int sir, rsir, gsir, bsir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;
        int wm = w - 1, hm = h - 1;
        
        int divsum = (radius + radius + 2) >> 1;
        divsum *= divsum;
        
        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                stack[i + radius] = p;
                rbs = r1 - Math.abs(i);
                rsir = (p & 0xff0000) >> 16;
                gsir = (p & 0x00ff00) >> 8;
                bsir = (p & 0x0000ff);
                rsum += rsir * rbs;
                gsum += gsir * rbs;
                bsum += bsir * rbs;
                if (i > 0) {
                    rinsum += rsir;
                    ginsum += gsir;
                    binsum += bsir;
                } else {
                    routsum += rsir;
                    goutsum += gsir;
                    boutsum += bsir;
                }
            }
            prt = radius;
            
            for (x = 0; x < w; x++) {
                rgb[yi] = ((rsum / divsum) << 16) | ((gsum / divsum) << 8) | (bsum / divsum);
                
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                
                start = prt - radius + div;
                sir = stack[start % div];
                
                routsum -= (sir & 0xFF0000) >> 16;
                goutsum -= (sir & 0x00FF00) >> 8;
                boutsum -= (sir & 0x0000FF);
                
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];
                
                stack[start % div] = p;
                rsir = (p & 0xff0000) >> 16;
                gsir = (p & 0x00ff00) >> 8;
                bsir = (p & 0x0000ff);
                
                rinsum += rsir;
                ginsum += gsir;
                binsum += bsir;
                
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                
                prt = (prt + 1) % div;
                sir = stack[prt % div];
                rsir = (sir & 0xff0000) >> 16;
                gsir = (sir & 0x00ff00) >> 8;
                bsir = (sir & 0x0000ff);
                
                routsum += rsir;
                goutsum += gsir;
                boutsum += bsir;
                
                rinsum -= rsir;
                ginsum -= gsir;
                binsum -= bsir;
                
                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                
                int c = rgb[yi];
                stack[i + radius] = c;
                
                rsir = (c & 0xFF0000) >> 16;
                gsir = (c & 0x00FF00) >> 8;
                bsir = (c & 0x0000FF);
                
                rbs = r1 - Math.abs(i);
                
                rsum += rsir * rbs;
                gsum += gsir * rbs;
                bsum += bsir * rbs;
                
                if (i > 0) {
                    rinsum += rsir;
                    ginsum += gsir;
                    binsum += bsir;
                } else {
                    routsum += rsir;
                    goutsum += gsir;
                    boutsum += bsir;
                }
                
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            prt = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | ((rsum / divsum) << 16) | ((gsum / divsum) << 8) | (bsum / divsum);
                
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                
                start = prt - radius + div;
                sir = stack[start % div];
                
                routsum -= (sir & 0xFF0000) >> 16;
                goutsum -= (sir & 0x00FF00) >> 8;
                boutsum -= (sir & 0x0000FF);
                
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];
                
                int c = rgb[p];
                stack[start % div] = c;
                
                rinsum += (c & 0xFF0000) >> 16;
                ginsum += (c & 0x00FF00) >> 8;
                binsum += (c & 0x0000FF);
                
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                
                prt = (prt + 1) % div;
                sir = stack[prt];
                
                rsir = (sir & 0xFF0000) >> 16;
                gsir = (sir & 0x00FF00) >> 8;
                bsir = (sir & 0x0000FF);
                
                routsum += rsir;
                goutsum += gsir;
                boutsum += bsir;
                
                rinsum -= rsir;
                ginsum -= gsir;
                binsum -= bsir;
                
                yi += w;
            }
        }
        
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
    }
}