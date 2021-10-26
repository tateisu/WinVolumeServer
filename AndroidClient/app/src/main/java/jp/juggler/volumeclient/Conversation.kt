package jp.juggler.volumeclient

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import jp.juggler.volumeclient.ui.theme.TestJetpackComposeTheme

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun ConversationPreview() {
    TestJetpackComposeTheme{
        Conversation(SampleData.conversationSample)
    }
}
