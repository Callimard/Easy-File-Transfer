package server.ftp.server;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import server.ftp.debug.Report;

public class FTPActionReminder {

	// Constants.

	// Variables.

	private ConcurrentHashMap<FTPClientConnection, List<Report>> hashConnectionAction = new ConcurrentHashMap<>();

	// Constructors.

	public FTPActionReminder() {
	}

	// Methods.

	public void addReportFor(FTPClientConnection ftpClientConnection, Report report) {
		List<Report> listReport = this.hashConnectionAction.get(ftpClientConnection);

		synchronized (ftpClientConnection) {

			if (listReport == null) {

				listReport = new Vector<>();

				this.hashConnectionAction.put(ftpClientConnection, listReport);
			}

			listReport.add(report);

		}
	}

	public List<Report> getListReportFor(FTPClientConnection ftpClientConnection) {

		List<Report> listReport = this.hashConnectionAction.get(ftpClientConnection);

		Collections.sort(listReport, new Comparator<Report>() {

			@Override
			public int compare(Report o1, Report o2) {

				if (o1.getCreationDate().equals(o2.getCreationDate())) {
					return 0;
				} else if (o1.getCreationDate().compareTo(o2.getCreationDate()) > 0) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		return listReport;
	}

}
