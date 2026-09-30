using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Stations;

public interface IStationRepository
{
    // Retrieves all solar stations from the database collection.
    Task<IEnumerable<SolarStationInfo>> GetAllStationsAsync();

    // Retrieves a solar station by its MongoDB ObjectId.
    Task<SolarStationInfo?> GetStationByIdAsync(string id);

    // Retrieves a solar station by its custom string identifier (e.g., ST001).
    Task<SolarStationInfo?> GetStationByStationIdAsync(string stationId);

    // Inserts a new solar station document into the database.
    Task CreateStationAsync(SolarStationInfo station);

    // Replaces an existing solar station document with updated information.
    Task UpdateStationAsync(string id, SolarStationInfo station);

    // Queries the database for stations within a specified geographic radius.
    Task<IEnumerable<SolarStationInfo>> GetNearbyStationsAsync(double latitude, double longitude, double radiusInKm);

    // Removes a solar station document from the database by its ObjectId.
    Task DeleteStationAsync(string id);
}
