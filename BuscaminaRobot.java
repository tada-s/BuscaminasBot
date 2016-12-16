package BuscaminasBot;

/**
 *
 * @author Tadashi
 */

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class BuscaminaRobot {
    
    public class Punto{
        public int x, y;
        public Punto(){
            this.x = 0;
            this.y = 0;
        }
        public Punto(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
    
    public class Pila<T> {
        private Node<T> raiz = null;

        public boolean estaVacio(){
            if(raiz == null){
                return true;
            }else{
                return false;
            }
        }

        public void insertarPila(T dato){
            Node<T> aux = new Node<T>(dato);
            aux.setNext(raiz);
            raiz = aux;
        }

        public void quitarPila(){
            raiz = raiz.getNext();
        }

        public T verPila(){
            return raiz.getData();
        }
    }
    
    public class Node <T>{
        private T data;
        private Node next;

        public Node(T d){
            data = d;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

    }
    
    Runnable esperar = new Runnable (){
        public void run(){
            try{                    
                Thread.sleep(100);                
            }catch(Exception e){                    
                System.out.println("No se ha podido pausar la tarea.");                
            }
        }
    };
    
    private Robot bot;
    
    private boolean esFinDeJuego;
    
    private boolean imagenEsCargado;
    private Punto puntoEncontrado;
    private Punto puntoCaraFeliz;
    private Punto puntoEsquina1;
    private Punto puntoEsquina2;
    private BufferedImage imagenPantalla;
    private BufferedImage imagenCaraFeliz;
    private BufferedImage imagenCaraXX;
    private BufferedImage imagenCaraFacha;
    private BufferedImage imagenCaraOO;
    private BufferedImage imagenEsquina1;
    private BufferedImage imagenEsquina2;
    
    private Pila<Punto> zonaAtacable;
    
    private Pila<Punto> tareaClickIzquierdo;
    private Pila<Punto> tareaClickDerecho;
    
    private int matriz[][];
    private int matrizVisitado[][];    
    private Punto matrizDimension;
    private final static int matrizVacio = 0;
    private final static int matrizVacioSuposicion = 10;
    private final static int matrizBomba = 99;
    private final static int matrizBombaSuposicion = 98;
    private final static int matrizDesconocido = 9;
    private final static int matrizBombaExploto = 100;
    
    private final static int clickDerecho = 1;
    private final static int clickIzquierdo = 2;
    
    private boolean clickAnimado;
    private boolean buscaminasDetectado;
    
    private int ax[] = {-1,0,1,1,1,0,-1,-1};
    private int ay[] = {-1,-1,-1,0,1,1,1,0};
    
    public BuscaminaRobot(){
        buscaminasDetectado = false;
        imagenEsCargado = false;
        puntoCaraFeliz = new Punto(-1,-1);
        puntoEsquina1 = new Punto(-1,-1);
        puntoEsquina2 = new Punto(-1,-1);
        try{
            imagenCaraFeliz = ImageIO.read(new File("SmileFace.bmp"));
            imagenCaraFacha = ImageIO.read(new File("YeahFace.bmp"));
            imagenCaraXX = ImageIO.read(new File("OMGFace.bmp"));
            imagenCaraOO = ImageIO.read(new File("OFace.bmp"));
            imagenEsquina1 = ImageIO.read(new File("Edge1.bmp"));
            imagenEsquina2 = ImageIO.read(new File("Edge2.bmp"));
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Error al cargar las imagenes.");
        }
        try{
            bot = new Robot();
        }catch(Exception e){}
        zonaAtacable = new Pila<>();
        tareaClickIzquierdo = new Pila<>();
        tareaClickDerecho = new Pila<>();
        clickAnimado = false;
    }
    
    public void jugarBuscaminas(){
        actualizarMatriz();
        while(!esGameOver()){
            actualizarZonaAtacable();
            ejecutarZonaAtacable();
            realizarClicks();
            actualizarMatriz();
            esperar.run();
        }
    }
    
    /* 
     * 
     *  Caputra de pantalla, movimiento de mouse.
     *
     */
    
    public boolean detectarBuscamina(){
        actualizarPantalla();
        if(!encuentraImagen(imagenPantalla,imagenCaraFeliz)) return false;
        puntoCaraFeliz = puntoEncontrado;
        if(!encuentraImagen(imagenPantalla,imagenEsquina1)) return false;
        puntoEsquina1 = puntoEncontrado;
        puntoEsquina1.x = puntoEsquina1.x+10;
        puntoEsquina1.y = puntoEsquina1.y+53;
        if(!encuentraImagen(imagenPantalla,imagenEsquina2)) return false;
        puntoEsquina2 = puntoEncontrado;
        
        matrizDimension = new Punto((puntoEsquina2.x-puntoEsquina1.x)/16 + 1,
                                    (puntoEsquina2.y-puntoEsquina1.y)/16 + 1);
        
        matriz = new int[matrizDimension.x][matrizDimension.y];
        matrizVisitado = new int[matrizDimension.x][matrizDimension.y];
        buscaminasDetectado = true;
        return true;
    }
    
    public boolean juegoDetectado(){
        return buscaminasDetectado;
    }
    
    public void verAnimacionClick(boolean b){
        clickAnimado = b;
    }
    
    public void actualizarPantalla(){
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        imagenPantalla = bot.createScreenCapture(screenRectangle);
    }
    
    private void actualizarMatriz(){
        actualizarPantalla();
        for(int i = 0; i < matrizDimension.x; i++)
            for(int j = 0; j < matrizDimension.y; j++)
                matriz[i][j] = getTipoCuadrado(i,j);
    }
    
    private int getTipoCuadrado(int x, int y){
        int color = imagenPantalla.getRGB(puntoEsquina1.x + x*16 + 7,
                                          puntoEsquina1.y + y*16 + 4);
        
        if(color == rgb(0,0,255)) return 1;
        if(color == rgb(0,128,0)) return 2;
        if(color == rgb(255,0,0)){
            color = imagenPantalla.getRGB(puntoEsquina1.x + x*16 + 1, puntoEsquina1.y + y*16 + 1);
            if(color == rgb(255,255,255)) return matrizBomba;
            else return 3;
        }
        if(color == rgb(0,0,128)) return 4;
        if(color == rgb(128,0,0)) return 5;
        if(color == rgb(0,128,128)) return 6;
        if(color == rgb(0,0,0)){
            color = imagenPantalla.getRGB(puntoEsquina1.x + x*16 + 2, puntoEsquina1.y + y*16 + 2);
            if(color == rgb(255,0,0)) return matrizBombaExploto;
            else return 7;
        }
        if(color == rgb(128,128,128)) return 8;
        if(color == rgb(192,192,192)){
            color = imagenPantalla.getRGB(puntoEsquina1.x + x*16, puntoEsquina1.y + y*16);
            if(color == rgb(255,255,255)) return matrizDesconocido;
            else return matrizVacio;
        }
        return -1;
    }
    
    private int rgb(int r, int g, int b){
        int c = -1;
        c = (c << 8) + r;
        c = (c << 8) + g;
        c = (c << 8) + b;
        return c;
    }
    
    private boolean encuentraImagen(BufferedImage imagenGrande, BufferedImage imagenChico){
        int sx = imagenChico.getWidth();
        int sy = imagenChico.getHeight();
        
        for(int i = 0; i < imagenGrande.getWidth() - sx; i++){
            for(int j = 0; j < imagenGrande.getHeight() - sy; j++){
                if(esIgualImagen(imagenGrande.getSubimage(i, j, sx, sy),imagenChico)){
                    puntoEncontrado = new Punto(i,j);
                    return true;
                }
            }
        }
        return false;
        
        
    }
    
    private boolean esIgualImagen(BufferedImage i1, BufferedImage i2){
        
        for(int i = 0; i < i2.getWidth(); i++)
            for(int j = 0; j < i2.getHeight(); j++)
                if(i1.getRGB(i, j) != i2.getRGB(i, j)) 
                    return false;
        
        return true;
    }
    
    private void realizarClicks(){
        click(0,-1,clickIzquierdo);
        if(tareaClickDerecho.estaVacio() && tareaClickIzquierdo.estaVacio()){
            casoNoTrivial();
            click(tareaClickIzquierdo.verPila(),clickIzquierdo);
            tareaClickIzquierdo.quitarPila();
        }else{
            while(!tareaClickDerecho.estaVacio()){
                click(tareaClickDerecho.verPila(),clickDerecho);
                tareaClickDerecho.quitarPila();
            }
            while(!tareaClickIzquierdo.estaVacio()){
                click(tareaClickIzquierdo.verPila(),clickIzquierdo);
                tareaClickIzquierdo.quitarPila();
            }
        }
    }
    
    private void casoNoTrivial(){
        int i = (int)(Math.random()*matrizDimension.x);
        int j = (int)(Math.random()*matrizDimension.y);
        while(matriz[i][j] != matrizDesconocido){
            i = (int)(Math.random()*matrizDimension.x);
            j = (int)(Math.random()*matrizDimension.y);
        }
        tareaClickIzquierdo.insertarPila(new Punto(i,j));
    }
    
    private void click(int x, int y, int boton){
        bot.mouseMove(puntoEsquina1.x + x*16 + 8,puntoEsquina1.y + y*16 + 8);
        switch(boton){
            case clickIzquierdo:
                bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                bot.mouseRelease(InputEvent.BUTTON1_MASK);
                break;
            case clickDerecho:
                bot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                bot.mouseRelease(InputEvent.BUTTON3_MASK);
                break;
        }
        if(clickAnimado) esperar.run();
    }
    
    private void click(Punto p, int boton){
        click(p.x,p.y,boton);
    }
    
    /*
     *
     *  Jugar Buscaminas
     *
     */

    
    private boolean esGameOver(){
        BufferedImage imagenCara = imagenPantalla.getSubimage(puntoCaraFeliz.x, puntoCaraFeliz.y, 24, 24);
        if(esIgualImagen(imagenCara,imagenCaraFeliz)){
            return false;
        }else if(esIgualImagen(imagenCara,imagenCaraOO)){
            return false;
        }
        return true;
    }
    
    private boolean estaDentroMatriz(int x, int y){
        if(x >= 0 && x < matrizDimension.x &&
           y >= 0 && y < matrizDimension.y)
            return true;
        else return false;
    }
    
    
    private void actualizarZonaAtacable(){
        for(int i = 0; i < matrizDimension.x; i++){
            for(int j = 0; j < matrizDimension.y; j++){
                if(matriz[i][j] == matrizDesconocido){
                    for(int c = 0; c < 8; c++){
                        if(estaDentroMatriz(i+ax[c],j+ay[c])){
                            if(0 < matriz[i+ax[c]][j+ay[c]] && matriz[i+ax[c]][j+ay[c]] < 9){
                                zonaAtacable.insertarPila(new Punto(i,j));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void ejecutarZonaAtacable(){
        Punto p;
        while(!zonaAtacable.estaVacio()){
            p = zonaAtacable.verPila();
            zonaAtacable.quitarPila();
            matriz[p.x][p.y] = matrizBombaSuposicion;
            if(existeContradiccionSuposicion(p.x,p.y)){
                tareaClickIzquierdo.insertarPila(new Punto(p.x,p.y));
            }else{
                matriz[p.x][p.y] = matrizVacioSuposicion;
                if(existeContradiccionSuposicion(p.x,p.y)){
                    tareaClickDerecho.insertarPila(new Punto(p.x,p.y));
                }
            }
            matriz[p.x][p.y] = matrizDesconocido;
        }
    }

    private boolean existeContradiccionSuposicion(int x, int y){
        for(int c = 0; c < 8; c++){
            if(estaDentroMatriz(x+ax[c],y+ay[c])){
                if(0 < matriz[x+ax[c]][y+ay[c]] && matriz[x+ax[c]][y+ay[c]] < 9){
                    if(existeContradiccionNumeros(x+ax[c],y+ay[c])){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean existeContradiccionNumeros(int x, int y){
        int vx[] = new int[8];
        int vy[] = new int[8];
        int c = 0;
        int cDesconocido = 0;
        int cBomba = 0;
        if(matrizVisitado[x][y] != 0) return false;
        
        matrizVisitado[x][y] = 1;
        for(int d = 0; d < 8 ; d++){
            if(estaDentroMatriz(x+ax[d],y+ay[d])){
                if(matriz[x+ax[d]][y+ay[d]] == matrizDesconocido){
                    vx[c] = x+ax[d];
                    vy[c] = y+ay[d];
                    c++;
                    cDesconocido++;
                }else if(matriz[x+ax[d]][y+ay[d]] == matrizBomba || matriz[x+ax[d]][y+ay[d]] == matrizBombaSuposicion){
                    cBomba++;
                }
            }
        }
        if(cDesconocido+cBomba < matriz[x][y] || cBomba > matriz[x][y]){               
            matrizVisitado[x][y] = 0;
            return true;
        }
        if(cDesconocido+cBomba == matriz[x][y]){
            for(int i = 0; i < c; i++){
                matriz[vx[i]][vy[i]] = matrizBombaSuposicion;
            }
            for(int i = 0; i < c; i++){
                if(existeContradiccionSuposicion(vx[i],vy[i])){
                    matrizVisitado[x][y] = 0;
                    for(int ii = 0; ii < c; ii++){
                        matriz[vx[ii]][vy[ii]] = matrizDesconocido;
                    }
                    return true;
                }
            }
            for(int i = 0; i < c; i++){
                matriz[vx[i]][vy[i]] = matrizDesconocido;
            }
        }
        if(cBomba == matriz[x][y]){
            for(int i = 0; i < c; i++){
                matriz[vx[i]][vy[i]] = matrizVacioSuposicion;
            }
            for(int i = 0; i < c; i++){
                if(existeContradiccionSuposicion(vx[i],vy[i])){
                    matrizVisitado[x][y] = 0;
                    for(int ii = 0; ii < c; ii++){
                        matriz[vx[ii]][vy[ii]] = matrizDesconocido;
                    }
                    return true;
                }
            }
            for(int i = 0; i < c; i++){
                matriz[vx[i]][vy[i]] = matrizDesconocido;
            }
        }
        matrizVisitado[x][y] = 0;
        return false;
    }

}