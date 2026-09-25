using MongoDB.Bson;
using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public class TransactionRepository : ITransactionRepository
{
    private readonly IMongoCollection<EnergyTransaction> _transactions;

    // Receives the shared MongoDB context and configures transaction indexes.
    public TransactionRepository(MongoDbContext dbContext)
    {
        _transactions = dbContext.EnergyTransactions;

        var indexes = new[]
        {
            new CreateIndexModel<EnergyTransaction>(
                Builders<EnergyTransaction>.IndexKeys.Ascending(t => t.TransactionId),
                new CreateIndexOptions { Unique = true }),
            new CreateIndexModel<EnergyTransaction>(
                Builders<EnergyTransaction>.IndexKeys.Ascending(t => t.ReservationId),
                new CreateIndexOptions { Unique = true }),
            new CreateIndexModel<EnergyTransaction>(
                Builders<EnergyTransaction>.IndexKeys.Ascending(t => t.QrToken),
                new CreateIndexOptions { Unique = true })
        };

        _transactions.Indexes.CreateMany(indexes);
    }

    // Finds one transaction using its MongoDB object ID or public transaction ID.
    public async Task<EnergyTransaction?> GetByIdOrTransactionIdAsync(string id)
    {
        if (ObjectId.TryParse(id, out _))
        {
            var byObjectId = await _transactions.Find(t => t.Id == id).FirstOrDefaultAsync();
            if (byObjectId != null)
            {
                return byObjectId;
            }
        }

        return await _transactions.Find(t => t.TransactionId == id).FirstOrDefaultAsync();
    }

    // Finds a transaction from the secure QR token scanned by the operator.
    public async Task<EnergyTransaction?> GetByQrTokenAsync(string qrToken)
    {
        return await _transactions.Find(t => t.QrToken == qrToken).FirstOrDefaultAsync();
    }

    // Finds the transaction generated for a specific approved reservation.
    public async Task<EnergyTransaction?> GetByReservationIdAsync(string reservationId)
    {
        return await _transactions.Find(t => t.ReservationId == reservationId).FirstOrDefaultAsync();
    }

    // Returns transfers that have not yet reached a terminal state.
    public async Task<IEnumerable<EnergyTransaction>> GetPendingAsync()
    {
        var filter = Builders<EnergyTransaction>.Filter.In(
            t => t.Status,
            new[] { TransactionStatus.Pending, TransactionStatus.Verified });

        return await _transactions.Find(filter).SortByDescending(t => t.VerifiedAt).ToListAsync();
    }

    // Returns completed energy transfers for operational history.
    public async Task<IEnumerable<EnergyTransaction>> GetCompletedAsync()
    {
        return await _transactions
            .Find(t => t.Status == TransactionStatus.Completed)
            .SortByDescending(t => t.CompletedAt)
            .ToListAsync();
    }

    // Persists a newly generated energy transaction.
    public async Task CreateAsync(EnergyTransaction transaction)
    {
        await _transactions.InsertOneAsync(transaction);
    }

    // Replaces a transaction after verification or completion.
    public async Task UpdateAsync(EnergyTransaction transaction)
    {
        if (string.IsNullOrWhiteSpace(transaction.Id))
        {
            throw new InvalidOperationException("Transaction database ID is missing.");
        }

        await _transactions.ReplaceOneAsync(t => t.Id == transaction.Id, transaction);
    }
}