package personnal.askinquery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.ExecutionException;

public class TraitementVideo {
    public static Bitmap getThumbnail(Uri Video, Context context) throws ExecutionException, InterruptedException {
        RequestOptions requestOptions = new RequestOptions();

        return Glide.with(context).setDefaultRequestOptions(requestOptions).asBitmap().load(Video).submit().get();
    }

}
