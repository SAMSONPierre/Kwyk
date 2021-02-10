public class Control {
    private Model model;
    private View view;
    
    Control(View view){
        this.view=view;
        this.model=this.view.getModel();
    }
}