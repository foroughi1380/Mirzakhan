package ir.mirzabazi.mirza.Game;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import cn.easyar.*;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class WidgetFinderViewPecker extends GLSurfaceView
{
    cn.easyar.Renderer renderer = new cn.easyar.Renderer();
    private boolean viewport_changed = false;
    private Vec2I view_size = new Vec2I(0, 0);
    private int rotation = 0;
    private Vec4I viewport = new Vec4I(0, 0, 1280, 720);
    private CameraDevice camera;
    private WidgetFinder finder;

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    boolean permission = true;
    public WidgetFinderViewPecker(Context context , @NonNull final WidgetFinder finder)
    {
        super(context);
        setEGLContextFactory(new ContextFactory());
        setEGLConfigChooser(new ConfigChooser());
        this.camera = finder.getCamera();
        this.finder = finder;
        this.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                updateViewport();
                getRootView().setDrawingCacheEnabled(true);
                buildDrawingCache(true);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                view_size = new Vec2I(w, h);
                viewport_changed = true;
                updateViewport();
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                if (permission){
                    Frame frame = finder.getStreamer().peek();
                    renderer.render(frame , viewport);
                    synchronized (frame){
                        frame.dispose();
                    }
                }
            }
        });
        this.setZOrderMediaOverlay(true);
    }

    private void updateViewport()
    {
        CameraCalibration calib = camera != null ? camera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.rotation) {
            this.rotation = rotation;
            viewport_changed = true;
        }
        if (viewport_changed) {
            Vec2I size = new Vec2I(1, 1);
            if ((camera != null) && camera.isOpened()) {
                size = camera.size();
            }
            if (rotation == 90 || rotation == 270) {
                size = new Vec2I(size.data[1], size.data[0]);
            }
            float scaleRatio = Math.max((float) view_size.data[0] / (float) size.data[0], (float) view_size.data[1] / (float) size.data[1]);
            Vec2I viewport_size = new Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio));
            viewport = new Vec4I((view_size.data[0] - viewport_size.data[0]) / 2, (view_size.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1]);

            if ((camera != null) && camera.isOpened())
                viewport_changed = false;
        }
    }

    public Image getLastImage(){
        int index = this.finder.getStreamer().peek().images().size() - 1;
        Image image = new Image();
        byte[] bytes = new byte[this.finder.getStreamer().peek().images().get(index).buffer().size()];
        this.finder.getStreamer().peek().images().get(index).buffer().copyTo(bytes , 0);
        image.setBytes(bytes);
        image.setWidth(this.finder.getStreamer().peek().images().get(index).width());
        image.setHeight(this.finder.getStreamer().peek().images().get(index).height());
        return  image;

        //byte[] ret = null;
        //this.finder.getStreamer().peek().images().get(this.finder.getStreamer().peek().images().size() - 1).buffer().copyTo(ret, 0);
        //return ( (ArrayList<cn.easyar.Image>)this.finder.getStreamer().peek().images().clone()).get(this.finder.getStreamer().peek().images().size() - 1);
        //return ret;
    }


    private static class ContextFactory implements GLSurfaceView.EGLContextFactory
    {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig)
        {
            EGLContext context;
            int[] attrib = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib );
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context)
        {
            egl.eglDestroyContext(display, context);
        }
    }

    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser
    {
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display)
        {
            final int EGL_OPENGL_ES2_BIT = 0x0004;
            final int[] attrib = { EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE };

            int[] num_config = new int[1];
            egl.eglChooseConfig(display, attrib, null, 0, num_config);

            int numConfigs = num_config[0];
            if (numConfigs <= 0)
                throw new IllegalArgumentException("fail to choose EGL configs");

            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, attrib, configs, numConfigs,
                    num_config);

            for (EGLConfig config : configs)
            {
                int[] val = new int[1];
                int r = 0, g = 0, b = 0, a = 0, d = 0;
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, val))
                    d = val[0];
                if (d < 16)
                    continue;

                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, val))
                    r = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, val))
                    g = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, val))
                    b = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, val))
                    a = val[0];
                if (r == 8 && g == 8 && b == 8 && a == 0)
                    return config;
            }

            return configs[0];
        }
    }

    public static class Image{

        private byte[] bytes;
        private int width = 0, height = 0;
        public Image(){}
        public Image(@NonNull byte[] bytes , @NonNull int width , @NonNull int height){
            this.bytes = bytes;
            this.width = width;
            this.height = height;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(@NonNull byte[] bytes) {
            this.bytes = bytes;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(@NonNull int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(@NonNull int height) {
            this.height = height;
        }
    }
}
