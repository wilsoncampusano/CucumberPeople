package org.cucumber.report;

import org.cucumberpeople.eclipse.plugin.common.report.ReportParser;
import org.cucumberpeople.eclipse.plugin.common.report.json.Feature;
import org.cucumberpeople.eclipse.plugin.common.report.json.Row;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.cucumber.report.FileReaderUtil.getAbsolutePathFromResource;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RowTest {

    ReportParser reportParser;
    Row row;

    @Before
    public void setUpJsonReports() throws IOException {
        List<String> jsonReports = new ArrayList<String>();
        jsonReports.add(getAbsolutePathFromResource("org/cucumber/report/cells.json"));
        reportParser = new ReportParser(jsonReports);
        Feature feature = reportParser.getFeatures().entrySet().iterator().next().getValue().get(0);
        row = feature.getElements().get(0).getSteps().get(0).getRows()[0];
        feature.processSteps();
    }

    @Test
    public void shouldReturnRows() {
        assertThat(row, is(Row.class));
    }

    @Test
    public void shouldReturnCells() {
        assertThat(row.getCells()[0], is("title"));
        assertThat(row.getCells()[1], is("lord"));
    }

}
