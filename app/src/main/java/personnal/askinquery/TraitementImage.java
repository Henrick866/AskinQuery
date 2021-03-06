package personnal.askinquery;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;

public class TraitementImage {
    static Bitmap RotateImage(Uri uri, Activity activity){
        try{
            Bitmap imageBase = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
        String FullPath = getRealPath(activity, uri);
        ExifInterface exifInterface = new ExifInterface(FullPath);
        Matrix matrix = new Matrix();
        switch(exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)){
            case 3 : matrix.postRotate(180);
                break;
            case 6: matrix.postRotate(90);
                break;
            case 8: matrix.postRotate(270);
                break;
        }
        return Bitmap.createBitmap(imageBase, 0, 0, imageBase.getWidth(), imageBase.getHeight(), matrix, true);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private static String getRealPath(Activity activity, Uri file){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        String Path;
        Cursor cursor = activity.getContentResolver().query(file, filePathColumn, null, null, null);
        if(cursor == null){
            return file.getPath();
        }else{
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            Path = cursor.getString(idx);
            cursor.close();
            return Path;
        }
    }
    static Bitmap createSquaredBitmap(Bitmap srcBmp, Context context) {//pour profil
        int dim = Math.max(srcBmp.getWidth(), srcBmp.getHeight());
        Bitmap dstBmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888), FinalBitmap = Bitmap.createBitmap(dstBmp);
        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.WHITE);
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 150 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        canvas.drawBitmap(srcBmp, (dim - srcBmp.getWidth()) / 2, (dim - srcBmp.getHeight()) / 2, null);
        FinalBitmap = Bitmap.createScaledBitmap(dstBmp, Math.round(px), Math.round(px), false);
        return FinalBitmap;
    }
    static Bitmap CreateThumbnail(Bitmap srcBmp, Context context, int DstHeight){
        int oHeight = srcBmp.getHeight(), oWidth = srcBmp.getWidth();
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dHeight = DstHeight * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        int dWidth = Math.round((dHeight * oWidth) / oHeight);
        return Bitmap.createScaledBitmap(srcBmp, dWidth, Math.round(dHeight), false);
    }
}
