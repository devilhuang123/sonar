package org.sonar.plugins.powershell.readers;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sonar.api.internal.apachecommons.io.input.BOMInputStream;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.issues.PsIssue;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IssuesReader {
	private static final Logger LOGGER = Loggers.get(IssuesReader.class);

	public List<PsIssue> read(final File file) throws Throwable {
		final List<PsIssue> issues = new LinkedList<>();
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final Document doc = builder.parse(new BOMInputStream(new FileInputStream(file)));
		final NodeList list = doc.getElementsByTagName("Object");
		for (int i = 0; i < list.getLength(); i++) {
			try {
				final Node node = list.item(i);
				final PsIssue issue = new PsIssue();
				issue.ruleId = getNodeByAttributeName(node, "RuleName").getTextContent();
				issue.message = getNodeByAttributeName(node, "Message").getTextContent();
				issue.line = Integer.parseInt(getNodeByAttributeName(node, "Line").getTextContent());
				issue.severity = getNodeByAttributeName(node, "Severity").getTextContent();
				issue.file = getNodeByAttributeName(node, "File").getTextContent();
				issues.add(issue);
			} catch (Exception e) {
				LOGGER.warn("Unexpected error reading results", e);
			}
		}
		return issues;
	}

	protected static final Node getNodeByAttributeName(final Node root, final String name) throws Exception {
		final NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final Node child = children.item(i);
			if (child.getAttributes() == null) {
				continue;
			}
			final Node attribute = child.getAttributes().getNamedItem("Name");
			if (attribute != null && attribute.getTextContent().equalsIgnoreCase(name)) {
				return child;
			}
		}
		throw new Exception("Child node with attribute named: '" + name + "' was not found");
	}
}