package experimentalcode.students.roedler.parallelCoordinates.visualizer.algorithm;

import java.util.ArrayList;
import java.util.Collection;

import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.elki.result.HierarchicalResult;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.result.ResultUtil;
import de.lmu.ifi.dbs.elki.utilities.DatabaseUtil;
import de.lmu.ifi.dbs.elki.visualization.VisualizationTask;
import de.lmu.ifi.dbs.elki.visualization.projector.ParallelPlotProjector;
import de.lmu.ifi.dbs.elki.visualization.visualizers.AbstractVisFactory;
import de.lmu.ifi.dbs.elki.visualization.visualizers.Visualization;
import de.lmu.ifi.dbs.elki.visualization.visualizers.parallel.AbstractParallelVisualization;
import experimentalcode.students.roedler.parallelCoordinates.gui.MenuOwner;
import experimentalcode.students.roedler.parallelCoordinates.gui.SubMenu;

/**
 * Arrange dimensions
 * 
 * @author Robert Rödler
 */
public class HSMDimensionOrder extends AbstractParallelVisualization<NumberVector<?, ?>> implements MenuOwner {
  /**
   * Generic tags to indicate the type of element. Used in IDs, CSS-Classes etc.
   */
  public static final String CLUSTERORDER = "HSM Dimension Order";

  private Clustering<Model> clustering;
  
  private int sum;
  
  private int mode = 3;

  /**
   * Constructor.
   * 
   * @param task VisualizationTask
   */
  public HSMDimensionOrder(VisualizationTask task) {
    super(task);
    clustering = task.getResult();
    sum = 0;
    incrementalRedraw();
  }

  @Override
  protected void redraw() {
  }

  private void arrange(int par) {
    int dim = DatabaseUtil.dimensionality(relation);
    Matrix hsmmat = new Matrix(dim, dim, 0.);
    Matrix pic = new Matrix(500, 500);

    DBIDs ids = null;
    
    switch(par){
      case -2: {
        ids = context.getSelection().getSelectedIds();
        break;
      }
      case -1: {
        ids = ResultUtil.getSamplingResult(relation).getSample();
        break;
      }
      default: {
        ids = clustering.getAllClusters().get(par).getIDs();
      }
    }
    
    int progress = 0;
    int max = ((dim * dim) - dim) / 2;
    
    for (int i = 1; i < dim; i++){
  //    if (!proj.isAxisVisible(i - 1)) { 
  //      continue; 
  //    }
      
      for (int j = i + 1; j <= dim; j++){
  //      if (!proj.isAxisVisible(j - 1)) { continue; }
        
        for(DBIDIter id = ids.iter(); id.valid(); id.advance()) {
          double[] yPos = proj.fastProjectDataToRenderSpace(relation.get(id));
          line(0, (int)(5. * yPos[i-1]), 499, (int)(5. * yPos[j-1]), pic);
        }

        Matrix hough = houghTransformation(pic);
        
        double median;
        
        median = (double)sum / (double)(hough.getColumnDimensionality() * hough.getRowDimensionality());
        
        double bigCells = (double) sumMatrix(splitMatrixIntoCells(hough, mode, median));
        
        hsmmat.set(i - 1, j - 1, 1. - (bigCells / 2500.));
        hsmmat.set(j - 1, i - 1, 1. - (bigCells / 2500.));
        
        progress++;
        System.out.println("HSM Progress " + progress + " von " + max);
      }
    }
    System.out.println(hsmmat.toString());
    
    ArrayList<Integer> arrange = new ArrayList<Integer>();

    int[] first = getMax(hsmmat);
    arrange.add(first[0]);
    arrange.add(1, first[1]);
    int[] pos;
    pos = first.clone();

    blank(hsmmat, pos[0]);
    blank(hsmmat, pos[1]);
    int tmp1, tmp2;

    for(int i = 2; i < dim; i++) {
      tmp1 = getMax(hsmmat, pos[0]);
      tmp2 = getMax(hsmmat, pos[1]);

      if(Math.abs(hsmmat.get(pos[0], tmp1)) > Math.abs(hsmmat.get(pos[1], tmp2))) {
        arrange.add(0, tmp1);
        blank(hsmmat, tmp1);
        pos[0] = tmp1;
      }
      else {
        arrange.add(arrange.size(), tmp2);
        blank(hsmmat, tmp2);
        pos[1] = tmp2;
      }
    }
    
    for(int i = 0; i < arrange.size(); i++) {
      System.out.print("" + arrange.get(i) + "  ");
    }

    for(int i = 0; i < dim; i++) {
      proj.moveAxis(proj.getAxisForDim(arrange.get(i)), i);
    }
  }
  
  private int sumMatrix(Matrix mat){
    int ret = 0;
    for (int i = 0; i < mat.getColumnDimensionality(); i++){
      for (int j = 0; j < mat.getRowDimensionality(); j++){
        if (mat.get(i, j) == 1.){
          ret++;
        }
      }
    }
    return ret;
  }
  
  private Matrix splitMatrixIntoCells(Matrix mat, int mode, double median){
    
    Matrix ret = new Matrix(50, 50, 0.);
    
    double stepX = mat.getColumnDimensionality() / 50.;
    double stepY = mat.getRowDimensionality() / 50.;
    
    for (int i = 0; i < 50; i++){
      for (int j = 0; j < 50; j++){
        
        double sum = 0.;
        int cells = 0;
        boolean bigger = true;
        
        for (int k = (int)(i * stepY); k < (int)((i + 1) * stepY); k++){
          for (int l = (int) (j * stepX); l < (int)((j + 1) * stepX); l++){
            
            if (mode == 1 && mat.get(k, l) > median){
              ret.set(i, j, 1.);
              break;
            }
            if (mode == 2 && mat.get(k, l) <= median){
              bigger = false;
              break;
            }
            sum += mat.get(k, l);
            cells++;
          }
        }
        if (mode == 2 && bigger){
          ret.set(i, j, 1.);
        }
        if (mode == 3 && ((sum /(double)cells) > median)){
          ret.set(i, j, 1.);
        }
      }
    } 
    return ret;
  }
  
  private Matrix houghTransformation(Matrix mat){
    
    Matrix ret;
    double theta;
    int d;
    //int max = (int) Math.sqrt(Math.pow((double) mat.getRowDimensionality() / 2., 2.) + Math.pow((double) mat.getColumnDimensionality() / 2., 2.));
    int max = (int) Math.sqrt(Math.pow((double) mat.getRowDimensionality(), 2.) + Math.pow((double) mat.getColumnDimensionality(), 2.));
    
    ret = new Matrix(max, 360, 0.);
    sum = 0;
    
    for (int x = 0; x < mat.getRowDimensionality(); x++){
      
      for (int y = 0; y < mat.getColumnDimensionality(); y++){
       
        if (mat.get(x, y) > 0){
          
          for (int ang = 0; ang < 360; ang++){
            
            theta = Math.toRadians(ang);
            
          //  d = (int)(i * Math.cos(theta) + j * Math.sin(theta));
            d = (int)(x * Math.cos(theta) + y * Math.sin(theta));
            
            if (d > 0 && d < max){
              ret.set(d, ang, ret.get(d, ang) + 1.);
              sum++;
            }
          }
        }
      }
    }
    
    return ret;
  }
  
  //Bresenham algorithm, copied from Wikipedia
  private void line(int x0, int y0, int x1, int y1, Matrix mat)
  {
    if(y0 == 500){y0--;}
    if(y1 == 500){y1--;}
    int dx =  Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
    int dy = -Math.abs(y1-y0), sy = y0<y1 ? 1 : -1; 
    int err = dx+dy, e2; /* error value e_xy */
   
    for(;;) { 
      mat.set(x0, y0, 1.);
      if (x0==x1 && y0==y1) break;
      
      e2 = 2*err;
      if (e2 > dy) { err += dy; x0 += sx; } /* e_xy+e_x > 0 */
      if (e2 < dx) { err += dx; y0 += sy; } /* e_xy+e_y < 0 */
    }
  }

  private int[] getMax(Matrix mat) {
    int[] pos = new int[2];
    double max = 0.;

    for(int i = 0; i < mat.getColumnDimensionality(); i++) {
      for(int j = 0; j < mat.getColumnDimensionality(); j++) {
        if(Math.abs(mat.get(i, j)) > max) {
          max = Math.abs(mat.get(i, j));
          pos[0] = i;
          pos[1] = j;
        }
      }
    }
    mat.set(pos[0], pos[1], 0.);
    mat.set(pos[1], pos[0], 0.);
    return pos;
  }

  private int getMax(Matrix mat, int row) {
    int pos = 0;
    double max = 0.;

    for(int i = 0; i < mat.getColumnDimensionality(); i++) {
      if(Math.abs(mat.get(row, i)) > max) {
        max = Math.abs(mat.get(row, i));
        pos = i;
      }
    }
    
    return pos;
  }

  private void blank(Matrix mat, int dim) {
    for(int i = 0; i < mat.getColumnDimensionality(); i++) {
      mat.set(i, dim, 0.);
    }
  }

  @Override
  public SubMenu getMenu() {
    SubMenu myMenu = new SubMenu(CLUSTERORDER, this);

    myMenu.addItem("arrange", Integer.toString(-1));

    int clus = clustering.getAllClusters().size();
    if(clus > 0) {
      for(int num = 0; num < clus; num++) {
        myMenu.addItem("arrange Cluster " + num, Integer.toString(num));
      }
    }
    myMenu.addItem("arrange selected", "-2");

    return myMenu;
  }

  @Override
  public void menuPressed(String id, boolean checked) {
    int iid = Integer.parseInt(id);
    arrange(iid);
    context.contentChanged(null);
  }

  /**
   * Factory
   * 
   * @author Robert Rödler
   * 
   * @apiviz.stereotype factory
   * @apiviz.uses AxisVisualization oneway - - «create»
   * 
   */
  public static class Factory extends AbstractVisFactory {
    /**
     * A short name characterizing this Visualizer.
     */
    private static final String NAME = "HSM Dimension Order";

    /**
     * Constructor, adhering to
     * {@link de.lmu.ifi.dbs.elki.utilities.optionhandling.Parameterizable}
     */
    public Factory() {
      super();
    }

    @Override
    public Visualization makeVisualization(VisualizationTask task) {
      return new HSMDimensionOrder(task);
    }

    @Override
    public void processNewResult(HierarchicalResult baseResult, Result result) {
      // Find clusterings we can visualize:
      Collection<Clustering<?>> clusterings = ResultUtil.filterResults(result, Clustering.class);
      for(Clustering<?> c : clusterings) {
        if(c.getAllClusters().size() > 0) {
          Collection<ParallelPlotProjector<?>> ps = ResultUtil.filterResults(baseResult, ParallelPlotProjector.class);
          for(ParallelPlotProjector<?> p : ps) {
            final VisualizationTask task = new VisualizationTask(NAME, c, p.getRelation(), this);
            task.put(VisualizationTask.META_LEVEL, VisualizationTask.LEVEL_DATA + 3);
            baseResult.getHierarchy().add(c, task);
            baseResult.getHierarchy().add(p, task);
          }
        }
      }
    }
  }
}

