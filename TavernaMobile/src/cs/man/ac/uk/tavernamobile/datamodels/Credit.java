package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "user")
public class Credit extends ElementBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 487693226708437235L;
	@Text
	protected String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
