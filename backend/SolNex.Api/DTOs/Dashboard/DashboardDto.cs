namespace SolNex.Api.DTOs.Dashboard;

using SolNex.Api.DTOs.Stations;

public class DashboardMetricsDto
{
    public int TotalStations { get; set; }
    public int ActiveStations { get; set; }
    public int InactiveStations { get; set; }
    public int TotalBatterySlots { get; set; }
    public int AvailableBatterySlots { get; set; }
}

public class DashboardDto
{
    public DashboardMetricsDto Metrics { get; set; } = new();
    public IEnumerable<StationDto> RecentStations { get; set; } = Array.Empty<StationDto>();
}
