import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Main {
    public static void main(String[] args) {
        List<Word> words = WordSegmenter.segWithStopWords("患者数字时间前农活劳累后出现腰部疼痛，休息后疼痛症状可缓解。院外行口服药物治疗后效果一般，数字时间前出现腰部伴左下肢疼痛。患者于我院腰椎MR平扫：1.腰4/5椎间盘膨出，腰5/骶1椎间盘膨出且突出并对应椎管狭窄 2.腰5/骶1椎间盘双侧侧隐窝受压变窄。患者为求进一步治疗，至我院就诊，经详细查体和询问病史后诊断为“腰椎间盘突出”,患者自发病来，无头晕、头痛，无胸闷、心慌，无咳嗽咳痰，无腹痛、腹泻，饮食可，因疼痛睡眠差，大小便正常，近期体重无明显增减");
        System.out.println("-------------------");
        for (Word word : words) {
            System.out.println(word);
        }
        System.out.println("分词数量" + words.size());
        Set<Word> set = new HashSet<>(words);
        System.out.println("去重后分词数量" + set.size());
    }
}