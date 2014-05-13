/*
 * Created on 20.10.2008
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sos.scheduler.consoleviews.events;

import java.util.HashMap;
import java.util.StringTokenizer;

import sos.util.string2bool.SOSBooleanExpression;
import sos.util.string2bool.SOSMalformedBooleanException;

public class BooleanExp {

	private String	boolExp;

	public BooleanExp(final String boolExp_) {
		super();
		boolExp = boolExp_;
		boolExp = boolExp.replaceAll(" and ", " && ");
		boolExp = boolExp.replaceAll("not ", "! ");
		boolExp = boolExp.replaceAll(" or ", " || ");
		boolExp = boolExp.replaceAll("[ ]{2,}", " "); // Viele Leerzeichen in 1 Leerzeichen
		boolExp = boolExp.trim();
	}

	public void replace(String s1, String s2) {
		boolExp = " " + boolExp + " ";
		boolExp = boolExp.replaceAll("\\(", " ( ");
		boolExp = boolExp.replaceAll("\\)", " ) ");

		s1 = " " + s1 + " ";
		s2 = " " + s2 + " ";
		boolExp = boolExp.replaceAll(s1, s2);
		boolExp = boolExp.replaceAll(" \\( ", "(");
		boolExp = boolExp.replaceAll(" \\) ", ")");
		boolExp = boolExp.trim();
	}

	public boolean evaluateExpression() {
		SOSBooleanExpression boolExpr = null;
		HashMap<String, String> allowedToken = new HashMap<String, String>();
		allowedToken.put("(", "");
		allowedToken.put(")", "");
		allowedToken.put("||", "");
		allowedToken.put("&&", "");
		allowedToken.put("!", "");
		allowedToken.put("!(", "");
		allowedToken.put(")!", "");
		allowedToken.put("(!)", "");
		allowedToken.put("true", "");
		allowedToken.put("false", "");
		allowedToken.put("(true", "");
		allowedToken.put("(false", "");
		allowedToken.put("(true)", "");
		allowedToken.put("(false)", "");
		allowedToken.put("true)", "");
		allowedToken.put("false)", "");

		try {
			String normalizedBoolExpr = "";
			StringTokenizer t = new StringTokenizer(boolExp, " ");
			while (t.hasMoreTokens()) {
				String s = t.nextToken();
				if (allowedToken.get(s) != null) {
					normalizedBoolExpr = normalizedBoolExpr + " " + s;
				}
				else {
					normalizedBoolExpr = normalizedBoolExpr + " false ";
				}
			}
			if (normalizedBoolExpr.length() == 0) {
				return true;
			}
			else {
				boolExpr = SOSBooleanExpression.readLeftToRight(normalizedBoolExpr);
				return boolExpr.booleanValue();
			}
		}
		catch (SOSMalformedBooleanException e) {
			System.out.println("--->" + boolExp);
			e.printStackTrace();
			return false;
		}
	}

	public String trueFalse(final boolean b) {
		if (b) {
			return " true ";
		}
		else {
			return " false ";
		}
	}

}
